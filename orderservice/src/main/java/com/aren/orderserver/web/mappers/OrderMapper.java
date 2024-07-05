package com.aren.orderserver.web.mappers;

import com.aren.orderserver.entities.Order;
import com.aren.orderserver.web.dto.OrderDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper extends Mappable<Order, OrderDto>{
}
