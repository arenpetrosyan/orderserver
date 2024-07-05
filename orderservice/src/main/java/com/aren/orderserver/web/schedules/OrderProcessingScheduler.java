package com.aren.orderserver.web.schedules;

import com.aren.orderserver.entities.Order;
import com.aren.orderserver.enums.OrderStatus;
import com.aren.orderserver.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class OrderProcessingScheduler {

    private final OrderService orderService;

    /**
     * Checks and returns orders to READY status if processing time is exceeded.
     */
    @Scheduled(fixedRate = 60000)
    public void checkAndReturnOrdersToReadyStatus() {
        List<Order> inProcessOrders = orderService.getInProcessOrders();

        for (Order order : inProcessOrders) {
            if (isProcessingTimeExceeded(order)) {
                order.setStatus(OrderStatus.READY.name());
                order.setProcessedBy(null);
                order.setUpdatedDate(null);
                orderService.updateOrder(order);
            }
        }
    }

    /**
     * Checks if processing time for an order has exceeded.
     *
     * @param order the order to check
     * @return true if processing time has exceeded, false otherwise
     */
    private boolean isProcessingTimeExceeded(Order order) {
        OffsetDateTime currentTime = OffsetDateTime.now();
        OffsetDateTime processingStartTime = order.getUpdatedDate();
        long processingTimeMinutes = 1L;

        return processingStartTime.plusMinutes(processingTimeMinutes).isBefore(currentTime);
    }

}
