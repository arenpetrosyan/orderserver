package com.aren.orderserver.web.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.aren.orderserver.entities.Order;
import com.aren.orderserver.entities.User;
import com.aren.orderserver.services.OrderService;
import com.aren.orderserver.web.dto.OrderDto;
import com.aren.orderserver.web.dto.StatisticsDto;
import com.aren.orderserver.web.mappers.OrderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ContextConfiguration(classes = {OrderController.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class OrderControllerTest {

    @Autowired
    private OrderController orderController;

    @MockBean
    private OrderMapper orderMapper;

    @MockBean
    private OrderService orderService;

    /**
     * Method under test: {@link OrderController#startProcessing(Integer)}
     */
    @Test
    void testStartProcessing() throws Exception {
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
        order.setDescription("Description");
        order.setId(1);
        order.setProcessedBy(processedBy);
        order.setStatus("Status");
        order.setTitle("title");
        order.setUpdatedDate(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        when(orderService.startProcessing(Mockito.<Integer>any())).thenReturn(order);

        OrderDto orderDto = new OrderDto();
        orderDto.setCreatedBy(createdBy);
        orderDto.setCreatedDate(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        orderDto.setDescription("Description");
        orderDto.setId(1);
        orderDto.setProcessedBy(processedBy);
        orderDto.setStatus("Status");
        orderDto.setTitle("title");
        orderDto.setUpdatedDate(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        when(orderMapper.toDto(Mockito.<Order>any())).thenReturn(orderDto);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.put("/api/v1/orders/{orderId}/start", 1);

        // Act and Assert
        MockMvcBuilders.standaloneSetup(orderController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content()
                        .string(
                                "{\"id\":1,\"title\":\"title\",\"description\":\"Description\",\"status\":\"Status\","
                                + "\"createdBy\":{\"id\":1,\"username\":\"username\",\"password\":\"password\",\"email\":\"test@mail.com\",\"role\""
                                + ":\"Role\"},\"processedBy\":{\"id\":1,\"username\":\"username\",\"password\":\"password\",\"email\":\"test@mail"
                                + ".com\",\"role\":\"Role\"},\"createdDate\":\"1970-01-01 00:00\",\"updatedDate\":\"1970-01-01 00:00\"}"));
    }

    /**
     * Method under test: {@link OrderController#completeProcessing(Integer)}
     */
    @Test
    void testCompleteProcessing() throws Exception {
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
        order.setDescription("Description");
        order.setId(1);
        order.setProcessedBy(processedBy);
        order.setStatus("Status");
        order.setTitle("title");
        order.setUpdatedDate(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        when(orderService.completeProcessing(Mockito.<Integer>any())).thenReturn(order);

        OrderDto orderDto = new OrderDto();
        orderDto.setCreatedBy(createdBy);
        orderDto.setCreatedDate(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        orderDto.setDescription("Description");
        orderDto.setId(1);
        orderDto.setProcessedBy(processedBy);
        orderDto.setStatus("Status");
        orderDto.setTitle("title");
        orderDto.setUpdatedDate(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        when(orderMapper.toDto(Mockito.<Order>any())).thenReturn(orderDto);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.put("/api/v1/orders/{orderId}/complete", 1);

        // Act and Assert
        MockMvcBuilders.standaloneSetup(orderController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content()
                        .string(
                                "{\"id\":1,\"title\":\"title\",\"description\":\"Description\",\"status\":\"Status\","
                                + "\"createdBy\":{\"id\":1,\"username\":\"username\",\"password\":\"password\",\"email\":\"test@mail.com\",\"role\""
                                + ":\"Role\"},\"processedBy\":{\"id\":1,\"username\":\"username\",\"password\":\"password\",\"email\":\"test@mail"
                                + ".com\",\"role\":\"Role\"},\"createdDate\":\"1970-01-01 00:00\",\"updatedDate\":\"1970-01-01 00:00\"}"));
    }

    /**
     * Method under test: {@link OrderController#getAllOrders()}
     */
    @Test
    void testGetAllOrders() throws Exception {
        // Arrange
        when(orderService.getOrders()).thenReturn(new ArrayList<>());
        when(orderMapper.toDto(Mockito.<List<Order>>any())).thenReturn(new ArrayList<>());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/v1/orders/all");

        // Act and Assert
        MockMvcBuilders.standaloneSetup(orderController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content().string("[]"));
    }

    /**
     * Method under test: {@link OrderController#getOrder(Integer)}
     */
    @Test
    void testGetOrder() throws Exception {
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
        order.setDescription("Description");
        order.setId(1);
        order.setProcessedBy(processedBy);
        order.setStatus("Status");
        order.setTitle("title");
        order.setUpdatedDate(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        when(orderService.getById(Mockito.<Integer>any())).thenReturn(order);

        OrderDto orderDto = new OrderDto();
        orderDto.setCreatedBy(createdBy);
        orderDto.setCreatedDate(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        orderDto.setDescription("Description");
        orderDto.setId(1);
        orderDto.setProcessedBy(processedBy);
        orderDto.setStatus("Status");
        orderDto.setTitle("title");
        orderDto.setUpdatedDate(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        when(orderMapper.toDto(Mockito.<Order>any())).thenReturn(orderDto);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/v1/orders/{orderId}", 1);

        // Act and Assert
        MockMvcBuilders.standaloneSetup(orderController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content()
                        .string(
                                "{\"id\":1,\"title\":\"title\",\"description\":\"Description\",\"status\":\"Status\","
                                + "\"createdBy\":{\"id\":1,\"username\":\"username\",\"password\":\"password\",\"email\":\"test@mail.com\",\"role\""
                                + ":\"Role\"},\"processedBy\":{\"id\":1,\"username\":\"username\",\"password\":\"password\",\"email\":\"test@mail"
                                + ".com\",\"role\":\"Role\"},\"createdDate\":\"1970-01-01 00:00\",\"updatedDate\":\"1970-01-01 00:00\"}"));
    }

    /**
     * Method under test: {@link OrderController#getReadyForProcessOrders()}
     */
    @Test
    void testGetReadyForProcessOrders() throws Exception {
        // Arrange
        when(orderService.getReadyToProcess()).thenReturn(new ArrayList<>());
        when(orderMapper.toDto(Mockito.<List<Order>>any())).thenReturn(new ArrayList<>());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/v1/orders/ready");

        // Act and Assert
        MockMvcBuilders.standaloneSetup(orderController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content().string("[]"));
    }

    /**
     * Method under test: {@link OrderController#getStatistics()}
     */
    @Test
    void testGetStatistics() throws Exception {
        // Arrange
        StatisticsDto builtitleesult = StatisticsDto.builder().inProcess(1L).processed(1L).ready(1L).total(1L).build();
        when(orderService.getStatisticsByUser()).thenReturn(builtitleesult);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/v1/orders/stat");

        // Act and Assert
        MockMvcBuilders.standaloneSetup(orderController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content().string("{\"processed\":1,\"inProcess\":1,\"ready\":1,\"total\":1}"));
    }

    /**
     * Method under test: {@link OrderController#placeAndOrder(OrderDto)}
     */
    @Test
    void testPlaceAndOrder() throws Exception {
        User createdBy = new User();
        createdBy.setEmail("test@mail.com");
        createdBy.setId(1);
        createdBy.setPassword("password");
        createdBy.setRole("Role");
        createdBy.setUsername("username");

        Order order = new Order();
        order.setCreatedBy(createdBy);
        order.setCreatedDate(OffsetDateTime.now());
        order.setDescription("Description");
        order.setId(1);
        order.setStatus("READY");
        order.setTitle("title");
        order.setUpdatedDate(OffsetDateTime.now());

        when(orderService.placeAndOrder(any())).thenReturn(order);

        OrderDto orderDto = new OrderDto();
        orderDto.setCreatedBy(createdBy);
        orderDto.setCreatedDate(OffsetDateTime.now());
        orderDto.setDescription("Description");
        orderDto.setStatus("READY");
        orderDto.setTitle("title");

        when(orderMapper.toDto(Mockito.<Order>any())).thenReturn(orderDto);
        when(orderMapper.toEntity(any())).thenReturn(order);

        OrderDto requestOrderDto = new OrderDto();
        requestOrderDto.setTitle("title");
        requestOrderDto.setDescription("Description");
        requestOrderDto.setCreatedBy(createdBy);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        String content = objectMapper.writeValueAsString(requestOrderDto);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1/orders/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);

        MockMvcBuilders.standaloneSetup(orderController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content()
                        .string(objectMapper.writeValueAsString(orderDto)));
    }
}
