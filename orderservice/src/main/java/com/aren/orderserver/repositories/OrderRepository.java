package com.aren.orderserver.repositories;

import com.aren.orderserver.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> getOrderByStatus(String status);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o WHERE o.id = :orderId AND o.createdBy.id = :userId")
    boolean isOrderOwner(@Param("userId") Integer userId, @Param("orderId") Integer orderId);

}
