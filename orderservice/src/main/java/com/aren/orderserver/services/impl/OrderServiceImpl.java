package com.aren.orderserver.services.impl;

import com.aren.orderserver.entities.Order;
import com.aren.orderserver.entities.User;
import com.aren.orderserver.enums.OrderStatus;
import com.aren.orderserver.enums.UserRole;
import com.aren.orderserver.exceptions.AccessDeniedException;
import com.aren.orderserver.exceptions.OrderProgressException;
import com.aren.orderserver.exceptions.ResourceNotFoundException;
import com.aren.orderserver.repositories.OrderRepository;
import com.aren.orderserver.services.OrderService;
import com.aren.orderserver.services.UserService;
import com.aren.orderserver.web.redis.OrderProcessingLock;
import com.aren.orderserver.web.dto.StatisticsDto;
import com.aren.orderserver.web.security.JwtEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final OrderProcessingLock orderProcessingLock;

    /**
     * Places and saves a new order.
     *
     * @param order The order to be placed
     * @return The placed order
     */
    @Override
    @Transactional
    public Order placeAndOrder(Order order) {
        order.setCreatedBy(getUser());
        order.setCreatedDate(OffsetDateTime.now());
        order.setStatus(OrderStatus.READY.name());
        return orderRepository.save(order);
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param orderId The ID of the order to retrieve
     * @return The order with the specified ID
     * @throws ResourceNotFoundException if the order is not found
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "OrderService::getById", key = "#orderId")
    public Order getById(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    /**
     * Retrieves a list of orders based on user roles.
     *
     * @return List of orders based on user roles
     * @throws AccessDeniedException if access is denied
     */
    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrders() {
        if (getUser().getRole().equals(UserRole.POSTER.name())) {
            return orderRepository.findAll().stream()
                    .filter(order -> order.getCreatedBy().getId().equals(getUser().getId()))
                    .toList();
        } else if (getUser().getRole().equals(UserRole.PROCESSOR.name())) {
            return orderRepository.findAll();
        } else {
            throw new AccessDeniedException("Access denied.");
        }
    }

    /**
     * Retrieves a list of orders ready to be processed.
     *
     * @return List of orders ready to be processed
     */
    @Override
    @Transactional(readOnly = true)
    public List<Order> getReadyToProcess() {
        return orderRepository.getOrderByStatus(OrderStatus.READY.name());
    }

    /**
     * Retrieves a list of orders in process.
     *
     * @return List of orders ready to be processed
     */
    @Override
    @Transactional(readOnly = true)
    public List<Order> getInProcessOrders() {
        return orderRepository.getOrderByStatus(OrderStatus.IN_PROCESS.name());
    }

    /**
     * Starts processing an order.
     *
     * @param orderId The ID of the order to start processing
     * @return The order after starting processing
     * @throws OrderProgressException if order is already processed or being processed
     */
    @Override
    @Transactional
    @CachePut(value = "OrderService::getById", key = "#orderId")
    public Order startProcessing(Integer orderId) {
        if (orderProcessingLock.isLocked(orderId)) {
            throw new OrderProgressException("Order already is processing by another user");
        }
        try {
            orderProcessingLock.acquireLock(orderId);
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));

            if (order.getStatus().equals(OrderStatus.PROCESSED.name())) {
                orderProcessingLock.releaseLock(orderId);
                throw new OrderProgressException("Order already processed");
            }

            if(order.getStatus().equals(OrderStatus.IN_PROCESS.name())) {
                orderProcessingLock.releaseLock(orderId);
                throw new OrderProgressException("Order already is processing by another user");
            }

            order.setStatus(OrderStatus.IN_PROCESS.name());
            order.setProcessedBy(getUser());
            order.setUpdatedDate(OffsetDateTime.now());

            return orderRepository.save(order);
        } finally {
            orderProcessingLock.releaseLock(orderId);
        }
    }

    /**
     * Completes processing of an order.
     *
     * @param orderId The ID of the order to complete processing
     * @return The order after completing processing
     * @throws OrderProgressException if order is already processed, not started, or being processed
     */
    @Override
    @Transactional
    @CachePut(value = "OrderService::getById", key = "#orderId")
    public Order completeProcessing(Integer orderId) {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getProcessedBy().equals(getUser())) {
            throw new OrderProgressException("You cannot complete processing for this order");
        }

        if (order.getStatus().equals(OrderStatus.PROCESSED.name())) {
            throw new OrderProgressException("Order already processed");
        }

        if (order.getStatus().equals(OrderStatus.READY.name())) {
            throw new OrderProgressException("Processing was not started.");
        }

        order.setProcessedBy(getUser());
        order.setUpdatedDate(OffsetDateTime.now());
        order.setStatus(OrderStatus.PROCESSED.name());
        return orderRepository.save(order);
    }

    /**
     * Retrieves statistics related to orders by user.
     *
     * @return StatisticsDto containing total, ready, processed, and in-process orders
     */
    @Override
    @Transactional(readOnly = true)
    public StatisticsDto getStatisticsByUser() {
        long total;
        long ready;
        long processed;
        long inProcess;

        total = orderRepository.count();
        ready = orderRepository.getOrderByStatus(OrderStatus.READY.name()).size();
        processed = orderRepository.getOrderByStatus(OrderStatus.PROCESSED.name()).size();
        inProcess = orderRepository.getOrderByStatus(OrderStatus.IN_PROCESS.name()).size();

        return StatisticsDto.builder()
                .total(total)
                .ready(ready)
                .processed(processed)
                .inProcess(inProcess)
                .build();
    }

    /**
     * Checks if a user is the owner of an order.
     *
     * @param userId  The ID of the user
     * @param orderId The ID of the order
     * @return true if the user is the owner of the order, false otherwise
     */
    @Transactional
    @Cacheable(value = "OrderService::getById", key = "#userId + '.' + #orderId")
    public boolean isOrderOwner(Integer userId, Integer orderId) {
        return orderRepository.isOrderOwner(userId, orderId);
    }

    /**
     * Updates the details of an order in the database.
     *
     * @param order The Order object containing the updated details
     */
    public void updateOrder(Order order) {
        orderRepository.save(order);
    }

    /**
     * Retrieves the current authenticated user.
     *
     * @return The current authenticated user
     */
    private User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtEntity jwtUser = (JwtEntity) authentication.getPrincipal();
        Integer userId = jwtUser.getId();
        return userService.getUser(userId);
    }
}
