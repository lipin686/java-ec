package com.example.demo.mapper;

import com.example.demo.dto.response.frontend.OrderItemResponse;
import com.example.demo.dto.response.frontend.OrderResponse;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 訂單Mapper
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    /**
     * 訂單實體轉換為訂單響應DTO
     */
    @Mapping(target = "statusDescription", source = "status.description")
    OrderResponse toOrderResponse(Order order);

    /**
     * 訂單實體列表轉換為訂單響應DTO列表
     */
    List<OrderResponse> toOrderResponseList(List<Order> orders);

    /**
     * 訂單項目實體轉換為訂單項目響應DTO
     */
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    /**
     * 訂單項目實體列表轉換為訂單項目響應DTO列表
     */
    List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> orderItems);
}

