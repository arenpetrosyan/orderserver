package com.aren.orderserver.services;

import com.aren.orderserver.entities.Order;
import com.aren.orderserver.web.dto.StatisticsDto;

import java.util.List;

public interface OrderService {

    Order placeAndOrder(Order order);

    Order getById(Integer orderId);

    List<Order> getOrders();

    List<Order> getReadyToProcess();

    List<Order> getInProcessOrders();

    Order startProcessing(Integer orderId);

    Order completeProcessing(Integer order);

    StatisticsDto getStatisticsByUser();

    boolean isOrderOwner(Integer userId, Integer orderId);

    void updateOrder(Order order);
}
