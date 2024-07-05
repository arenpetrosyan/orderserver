package com.aren.orderserver.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.aren.orderserver.entities.Order;
import com.aren.orderserver.entities.User;
import com.aren.orderserver.enums.OrderStatus;
import com.aren.orderserver.exceptions.AccessDeniedException;
import com.aren.orderserver.exceptions.OrderProgressException;
import com.aren.orderserver.exceptions.ResourceNotFoundException;
import com.aren.orderserver.repositories.OrderRepository;
import com.aren.orderserver.services.UserService;
import com.aren.orderserver.web.redis.OrderProcessingLock;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.aren.orderserver.web.security.JwtEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {OrderServiceImpl.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class OrderServiceImplTest {

    @MockBean
    private OrderProcessingLock orderProcessingLock;

    @MockBean
    private OrderRepository orderRepository;

    @Autowired
    private OrderServiceImpl orderServiceImpl;

    @MockBean
    private UserService userService;

    /**
     * Method under test: {@link OrderServiceImpl#placeAndOrder(Order)}
     */
    @Test
    void testPlaceAndOrder() {
        Order order = new Order();
        order.setId(1);
        order.setCreatedDate(OffsetDateTime.now());
        order.setStatus("Role");
        User user = new User();
        user.setId(1);
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("test@mail.com");

        when(userService.getUser(anyInt())).thenReturn(user);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        List<GrantedAuthority> authorities = new ArrayList<>();
        JwtEntity jwtEntity = new JwtEntity(user.getId(), user.getUsername(), user.getPassword(), user.getEmail(), authorities);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwtEntity);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        Order placedOrder = orderServiceImpl.placeAndOrder(order);

        // Assert
        assertNotNull(placedOrder);
        assertEquals(1, placedOrder.getId());
        assertEquals(OrderStatus.READY.name(), placedOrder.getStatus());
        assertEquals(user, placedOrder.getCreatedBy());

        // Verify interactions
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(userService, times(1)).getUser(anyInt());
    }

    /**
     * Method under test: {@link OrderServiceImpl#getById(Integer)}
     */
    @Test
    void testGetById() {
        // Arrange
        User createdBy = new User();
        createdBy.setEmail("test@mail.com");
        createdBy.setId(1);
        createdBy.setPassword("password");
        createdBy.setRole("Role");
        createdBy.setUsername("username");

        User processedBy = new User();
        processedBy.setEmail("test@mail.com");
        processedBy.setId(1);
        processedBy.setPassword("password");
        processedBy.setRole("Role");
        processedBy.setUsername("username");

        Order order = new Order();
        order.setCreatedBy(createdBy);
        order.setCreatedDate(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        order.setDescription("description");
        order.setId(1);
        order.setProcessedBy(processedBy);
        order.setStatus("Status");
        order.setTitle("Title");
        order.setUpdatedDate(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        Optional<Order> ofResult = Optional.of(order);
        when(orderRepository.findById(any())).thenReturn(ofResult);

        // Act
        Order actualById = orderServiceImpl.getById(1);

        // Assert
        verify(orderRepository).findById(eq(1));
        LocalTime expectedToLocalTimeResult = actualById.getUpdatedDate().toLocalDateTime().toLocalTime();
        assertSame(expectedToLocalTimeResult, actualById.getCreatedDate().toLocalDateTime().toLocalTime());
    }

    /**
     * Method under test: {@link OrderServiceImpl#getById(Integer)}
     */
    @Test
    void testGetByIdResourceNotFound() {
        // Arrange
        when(orderRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> orderServiceImpl.getById(1));
        verify(orderRepository).findById(eq(1));
    }


    /**
     * Method under test: {@link OrderServiceImpl#getById(Integer)}
     */
    @Test
    void testGetByIdAccessDenied() {
        // Arrange
        when(orderRepository.findById(anyInt())).thenThrow(new AccessDeniedException("An error occurred"));

        // Act and Assert
        assertThrows(AccessDeniedException.class, () -> orderServiceImpl.getById(1));
        verify(orderRepository).findById(eq(1));
    }

    /**
     * Method under test: {@link OrderServiceImpl#getOrders()}
     */
    @Test
    void testGetOrders() {
        // Arrange
        User user = new User();
        user.setId(1);
        user.setRole("PROCESSOR");
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("test@mail.com");

        List<Order> orders = new ArrayList<>();
        Order order1 = new Order();
        order1.setId(1);
        order1.setCreatedBy(user);
        orders.add(order1);

        when(userService.getUser(anyInt())).thenReturn(user);
        when(orderRepository.findAll()).thenReturn(orders);

        List<GrantedAuthority> authorities = new ArrayList<>();
        JwtEntity jwtEntity = new JwtEntity(user.getId(), user.getUsername(), user.getPassword(), user.getEmail(), authorities);
        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(jwtEntity);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        List<Order> retrievedOrders = orderServiceImpl.getOrders();

        // Assert
        assertNotNull(retrievedOrders);
        assertEquals(1, retrievedOrders.size());
        assertEquals(1, retrievedOrders.get(0).getId());

        // Verify interactions
        verify(userService, times(2)).getUser(anyInt());
        verify(orderRepository, times(1)).findAll();
    }

    /**
     * Method under test: {@link OrderServiceImpl#getReadyToProcess()}
     */
    @Test
    void testGetReadyToProcess() {
        // Arrange
        ArrayList<Order> orderList = new ArrayList<>();
        when(orderRepository.getOrderByStatus(any())).thenReturn(orderList);

        // Act
        List<Order> actualReadyToProcess = orderServiceImpl.getReadyToProcess();

        // Assert
        verify(orderRepository).getOrderByStatus(eq("READY"));
        assertTrue(actualReadyToProcess.isEmpty());
        assertSame(orderList, actualReadyToProcess);
    }

    /**
     * Method under test: {@link OrderServiceImpl#getReadyToProcess()}
     */
    @Test
    void testGetReadyToProcessWithOrders() {
        // Arrange
        List<Order> orders = new ArrayList<>();
        Order order1 = new Order();
        order1.setId(1);
        order1.setStatus(OrderStatus.READY.name());
        orders.add(order1);

        when(orderRepository.getOrderByStatus(OrderStatus.READY.name())).thenReturn(orders);

        // Act
        List<Order> readyOrders = orderServiceImpl.getReadyToProcess();

        // Assert
        assertNotNull(readyOrders);
        assertEquals(1, readyOrders.size());
        assertEquals(OrderStatus.READY.name(), readyOrders.get(0).getStatus());

        // Verify interactions
        verify(orderRepository, times(1)).getOrderByStatus(OrderStatus.READY.name());
    }

    /**
     * Method under test: {@link OrderServiceImpl#getReadyToProcess()}
     */
    @Test
    void testGetReadyToProcess_AccessDenied() {
        // Arrange
        when(orderRepository.getOrderByStatus(any())).thenThrow(new AccessDeniedException("An error occurred"));

        // Act and Assert
        assertThrows(AccessDeniedException.class, () -> orderServiceImpl.getReadyToProcess());
        verify(orderRepository).getOrderByStatus(eq("READY"));
    }

    /**
     * Method under test: {@link OrderServiceImpl#getInProcessOrders()}
     */
    @Test
    void testGetInProcessOrders() {
        // Arrange
        ArrayList<Order> orderList = new ArrayList<>();
        when(orderRepository.getOrderByStatus(any())).thenReturn(orderList);

        // Act
        List<Order> actualInProcessOrders = orderServiceImpl.getInProcessOrders();

        // Assert
        verify(orderRepository).getOrderByStatus(eq("IN_PROCESS"));
        assertTrue(actualInProcessOrders.isEmpty());
        assertSame(orderList, actualInProcessOrders);
    }

    /**
     * Method under test: {@link OrderServiceImpl#getInProcessOrders()}
     */
    @Test
    void testGetInProcessOrdersWithOrders() {
        // Arrange
        List<Order> orders = new ArrayList<>();
        Order order1 = new Order();
        order1.setId(1);
        order1.setStatus(OrderStatus.IN_PROCESS.name());
        orders.add(order1);

        when(orderRepository.getOrderByStatus(OrderStatus.IN_PROCESS.name())).thenReturn(orders);

        // Act
        List<Order> inProcessOrders = orderServiceImpl.getInProcessOrders();

        // Assert
        assertNotNull(inProcessOrders);
        assertEquals(1, inProcessOrders.size());
        assertEquals(OrderStatus.IN_PROCESS.name(), inProcessOrders.get(0).getStatus());

        // Verify interactions
        verify(orderRepository, times(1)).getOrderByStatus(OrderStatus.IN_PROCESS.name());
    }


    /**
     * Method under test: {@link OrderServiceImpl#getInProcessOrders()}
     */
    @Test
    void testGetInProcessOrdersAccessDenied() {
        // Arrange
        when(orderRepository.getOrderByStatus(any())).thenThrow(new AccessDeniedException("An error occurred"));

        // Act and Assert
        assertThrows(AccessDeniedException.class, () -> orderServiceImpl.getInProcessOrders());
        verify(orderRepository).getOrderByStatus(eq("IN_PROCESS"));
    }

    /**
     * Method under test: {@link OrderServiceImpl#startProcessing(Integer)}
     */
    @Test
    void testStartProcessing() {
        // Arrange
        Integer orderId = 1;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.READY.name());

        User user = new User();
        user.setId(1);
        user.setRole("PROCESSOR");
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("test@mail.com");
        order.setProcessedBy(user);

        List<GrantedAuthority> authorities = new ArrayList<>();
        JwtEntity jwtEntity = new JwtEntity(user.getId(), user.getUsername(), user.getPassword(), user.getEmail(), authorities);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwtEntity);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderProcessingLock.isLocked(orderId)).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(userService.getUser(anyInt())).thenReturn(user);

        // Act
        Order startedOrder = orderServiceImpl.startProcessing(orderId);

        // Assert
        assertNotNull(startedOrder);
        assertEquals(orderId, startedOrder.getId());
        assertEquals(OrderStatus.IN_PROCESS.name(), startedOrder.getStatus());
        assertEquals(user, startedOrder.getProcessedBy());

        // Verify interactions
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderProcessingLock, times(1)).isLocked(orderId);
        verify(orderProcessingLock, times(1)).acquireLock(orderId);
        verify(orderProcessingLock, times(1)).releaseLock(orderId);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    /**
     * Method under test: {@link OrderServiceImpl#startProcessing(Integer)}
     */
    @Test
    void testStartProcessingIsLocked() {
        // Arrange
        when(orderProcessingLock.isLocked(any())).thenReturn(true);

        // Act and Assert
        assertThrows(OrderProgressException.class, () -> orderServiceImpl.startProcessing(1));
        verify(orderProcessingLock).isLocked(eq(1));
    }

    /**
     * Method under test: {@link OrderServiceImpl#startProcessing(Integer)}
     */
    @Test
    void testStartProcessingResourceNotFound() {
        // Arrange
        when(orderProcessingLock.isLocked(any()))
                .thenThrow(new ResourceNotFoundException("An error occurred"));

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> orderServiceImpl.startProcessing(1));
        verify(orderProcessingLock).isLocked(eq(1));
    }

    /**
     * Method under test: {@link OrderServiceImpl#completeProcessing(Integer)}
     */
    @Test
    void testCompleteProcessing() {
        // Arrange
        Integer orderId = 1;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.IN_PROCESS.name());

        User user = new User();
        user.setId(1);
        user.setRole("PROCESSOR");
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("test@mail.com");
        order.setProcessedBy(user);

        List<GrantedAuthority> authorities = new ArrayList<>();
        JwtEntity jwtEntity = new JwtEntity(user.getId(), user.getUsername(), user.getPassword(), user.getEmail(), authorities);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwtEntity);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userService.getUser(anyInt())).thenReturn(user);
        when(orderProcessingLock.isLocked(orderId)).thenReturn(false);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        Order completedOrder = orderServiceImpl.completeProcessing(orderId);

        // Assert
        assertNotNull(completedOrder);
        assertEquals(orderId, completedOrder.getId());
        assertEquals(OrderStatus.PROCESSED.name(), completedOrder.getStatus());
        assertEquals(user, completedOrder.getProcessedBy());

        // Verify interactions
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    /**
     * Method under test: {@link OrderServiceImpl#completeProcessing(Integer)}
     */
    @Test
    void testCompleteProcessingEmpty() {
        // Arrange
        Optional<Order> emptyResult = Optional.empty();
        when(orderRepository.findById(any())).thenReturn(emptyResult);

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> orderServiceImpl.completeProcessing(1));
        verify(orderRepository).findById(eq(1));
    }

    /**
     * Method under test: {@link OrderServiceImpl#completeProcessing(Integer)}
     */
    @Test
    void testCompleteProcessingThrowOrderProgressExceptionWhenOrderIsAlreadyProcessed() {
        // Arrange
        Integer orderId = 1;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PROCESSED.name());

        User user = new User();
        user.setId(1);
        user.setRole("PROCESSOR");
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("test@mail.com");
        order.setProcessedBy(user);

        List<GrantedAuthority> authorities = new ArrayList<>();
        JwtEntity jwtEntity = new JwtEntity(user.getId(), user.getUsername(), user.getPassword(), user.getEmail(), authorities);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwtEntity);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userService.getUser(anyInt())).thenReturn(user);
        when(orderProcessingLock.isLocked(orderId)).thenReturn(false);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act and Assert
        OrderProgressException exception = assertThrows(OrderProgressException.class, () -> orderServiceImpl.completeProcessing(orderId));
        assertEquals("Order already processed", exception.getMessage());

        // Verify interactions
        verify(orderRepository, times(1)).findById(orderId);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void testCompleteProcessingShouldThrowOrderProgressExceptionWhenOrderProcessingNotStarted() {
        // Arrange
        Integer orderId = 1;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.READY.name());
        User user = new User();
        user.setId(1);
        user.setRole("PROCESSOR");
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("test@mail.com");
        order.setProcessedBy(user);

        List<GrantedAuthority> authorities = new ArrayList<>();
        JwtEntity jwtEntity = new JwtEntity(user.getId(), user.getUsername(), user.getPassword(), user.getEmail(), authorities);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwtEntity);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userService.getUser(anyInt())).thenReturn(user);
        when(orderProcessingLock.isLocked(orderId)).thenReturn(false);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act and Assert
        OrderProgressException exception = assertThrows(OrderProgressException.class, () -> orderServiceImpl.completeProcessing(orderId));
        assertEquals("Processing was not started.", exception.getMessage());

        // Verify interactions
        verify(orderRepository, times(1)).findById(orderId);
        verifyNoMoreInteractions(orderRepository);
    }

    /**
     * Method under test: {@link OrderServiceImpl#completeProcessing(Integer)}
     */
    @Test
    void testCompleteProcessingAccessDenied() {
        // Arrange
        when(orderRepository.findById(any())).thenThrow(new AccessDeniedException("An error occurred"));

        // Act and Assert
        assertThrows(AccessDeniedException.class, () -> orderServiceImpl.completeProcessing(1));
        verify(orderRepository).findById(eq(1));
    }


    /**
     * Method under test: {@link OrderServiceImpl#getStatisticsByUser()}
     */
    @Test
    void testGetStatisticsAccessDenied() {
        // Arrange
        when(orderRepository.count()).thenThrow(new AccessDeniedException("An error occurred"));

        // Act and Assert
        assertThrows(AccessDeniedException.class, () -> orderServiceImpl.getStatisticsByUser());
        verify(orderRepository).count();
    }

    /**
     * Method under test: {@link OrderServiceImpl#getStatisticsByUser()}
     */
    @Test
    void testGetStatisticsResourceNotFound() {
        // Arrange
        when(orderRepository.count()).thenThrow(new ResourceNotFoundException("An error occurred"));

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> orderServiceImpl.getStatisticsByUser());
        verify(orderRepository).count();
    }

    /**
     * Method under test: {@link OrderServiceImpl#isOrderOwner(Integer, Integer)}
     */
    @Test
    void testIsOrderOwner() {
        // Arrange
        when(orderRepository.isOrderOwner(any(), any())).thenReturn(true);

        // Act
        boolean actualIsOrderOwnerResult = orderServiceImpl.isOrderOwner(1, 1);

        // Assert
        verify(orderRepository).isOrderOwner(eq(1), eq(1));
        assertTrue(actualIsOrderOwnerResult);
    }

    /**
     * Method under test: {@link OrderServiceImpl#isOrderOwner(Integer, Integer)}
     */
    @Test
    void testIsOrderOwnerAccessDenied() {
        // Arrange
        when(orderRepository.isOrderOwner(any(), any()))
                .thenThrow(new AccessDeniedException("An error occurred"));

        // Act and Assert
        assertThrows(AccessDeniedException.class, () -> orderServiceImpl.isOrderOwner(1, 1));
        verify(orderRepository).isOrderOwner(eq(1), eq(1));
    }

    /**
     * Method under test: {@link OrderServiceImpl#updateOrder(Order)}
     */
    @Test
    void testUpdateOrder() {
        // Arrange
        User createdBy = new User();
        createdBy.setEmail("test@mail.com");
        createdBy.setId(1);
        createdBy.setPassword("password");
        createdBy.setRole("Role");
        createdBy.setUsername("username");

        User processedBy = new User();
        processedBy.setEmail("test@mail.com");
        processedBy.setId(2);
        processedBy.setPassword("password");
        processedBy.setRole("Role");
        processedBy.setUsername("username");

        Order order = new Order();
        order.setCreatedBy(createdBy);
        order.setCreatedDate(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        order.setDescription("description");
        order.setId(1);
        order.setProcessedBy(processedBy);
        order.setStatus("Status");
        order.setTitle("Title");
        order.setUpdatedDate(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        when(orderRepository.save(any())).thenReturn(order);

        User createdBy2 = new User();
        createdBy2.setEmail("test@mail.com");
        createdBy2.setId(1);
        createdBy2.setPassword("password");
        createdBy2.setRole("Role");
        createdBy2.setUsername("username");

        User processedBy2 = new User();
        processedBy2.setEmail("test@mail.com");
        processedBy2.setId(1);
        processedBy2.setPassword("password");
        processedBy2.setRole("Role");
        processedBy2.setUsername("username");

        Order order2 = new Order();
        order2.setCreatedBy(createdBy2);
        order2.setCreatedDate(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        order2.setDescription("description");
        order2.setId(1);
        order2.setProcessedBy(processedBy2);
        order2.setStatus("Status");
        order2.setTitle("Title");
        order2.setUpdatedDate(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));

        // Act
        orderServiceImpl.updateOrder(order2);

        // Assert that nothing has changed
        verify(orderRepository).save(isA(Order.class));
        assertEquals("Title", order2.getTitle());
        assertEquals("Status", order2.getStatus());
        assertEquals("description", order2.getDescription());
        assertEquals("Z", order2.getCreatedDate().getOffset().toString());
        assertEquals("Z", order2.getUpdatedDate().getOffset().toString());
        assertEquals("test@mail.com", order2.getCreatedBy().getEmail());
        assertEquals("test@mail.com", order2.getProcessedBy().getEmail());
        assertEquals(1, order2.getId().intValue());
    }
}
