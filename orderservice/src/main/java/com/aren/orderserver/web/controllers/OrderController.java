package com.aren.orderserver.web.controllers;

import com.aren.orderserver.entities.Order;
import com.aren.orderserver.services.OrderService;
import com.aren.orderserver.web.validation.OnCreate;
import com.aren.orderserver.web.dto.OrderDto;
import com.aren.orderserver.web.dto.StatisticsDto;
import com.aren.orderserver.web.mappers.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/v1/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    /**
     * Endpoint to place a new order.
     * Requires the user to have the role of a poster.
     *
     * @param orderDto The OrderDto object containing order details
     * @return The created OrderDto after placing the order
     */
    @PostMapping(value = "/")
    @PreAuthorize("@customSecurityExpression.canPostOrder()")
    public OrderDto placeAndOrder(@Validated(OnCreate.class) @RequestBody OrderDto orderDto){
        Order order = orderMapper.toEntity(orderDto);
        Order placedOrder = orderService.placeAndOrder(order);
        return orderMapper.toDto(placedOrder);
    }

    /**
     * Endpoint to retrieve all orders.
     * User with the role of a poster will receive only own created orders.
     * User with the role of a processors will receive all orders
     *
     * @return List of OrderDto objects representing all orders
     */
    @GetMapping(value = "/all")
    public List<OrderDto> getAllOrders(){
        List<Order> orders = orderService.getOrders();
        return orderMapper.toDto(orders);
    }

    /**
     * Endpoint to retrieve an order by its ID.
     * Requires the user to have the role of a processor or be the owner of the order.
     *
     * @param orderId The ID of the order to retrieve
     * @return The OrderDto object representing the requested order
     */
    @GetMapping(value = "/{orderId}")
    @PreAuthorize("@customSecurityExpression.canGetOrder(#orderId)")
    public OrderDto getOrder(@PathVariable Integer orderId){
        Order order = orderService.getById(orderId);
        return orderMapper.toDto(order);
    }

    /**
     * Endpoint to retrieve all orders that are ready for processing.
     * Requires the user to have the role of a processor.
     *
     * @return List of OrderDto objects representing orders ready for processing
     */
    @GetMapping(value = "/ready")
    @PreAuthorize("@customSecurityExpression.canProcessOrder()")
    public List<OrderDto> getReadyForProcessOrders(){
        List<Order> orders = orderService.getReadyToProcess();
        return orderMapper.toDto(orders);
    }

    /**
     * Endpoint to start processing an order by its ID.
     * Requires the user to have the role of a processor.
     *
     * @param orderId The ID of the order to start processing
     * @return The updated OrderDto after starting processing
     */
    @PutMapping(value = "/{orderId}/start")
    @PreAuthorize("@customSecurityExpression.canProcessOrder()")
    public OrderDto startProcessing(@PathVariable Integer orderId){
        Order updated = orderService.startProcessing(orderId);
        return orderMapper.toDto(updated);
    }

    /**
     * Endpoint to complete processing of an order by its ID.
     * Requires the user to have the role of a processor.
     *
     * @param orderId The ID of the order to complete processing
     * @return The updated OrderDto after completing processing
     */
    @PutMapping(value = "/{orderId}/complete")
    @PreAuthorize("@customSecurityExpression.canProcessOrder()")
    public OrderDto completeProcessing(@PathVariable Integer orderId){
        Order updated = orderService.completeProcessing(orderId);
        return orderMapper.toDto(updated);
    }

    /**
     * Endpoint to retrieve statistics related to orders by the current user.
     * Requires the user to have the role of a processor.
     *
     * @return StatisticsDto containing total, ready, processed, and in-process orders
     */
    @GetMapping(value = "/stat")
    @PreAuthorize("@customSecurityExpression.canProcessOrder()")
    public StatisticsDto getStatistics(){
        return orderService.getStatisticsByUser();
    }

}
