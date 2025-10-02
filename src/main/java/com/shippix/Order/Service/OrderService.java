package com.shippix.Order.Service;

import com.shippix.Order.DTO.OrderResponse;
import com.shippix.Order.Model.Order;
import com.shippix.Order.Repo.OrderRepo;
import com.shippix.User_Management.Model.Users;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepo orderRepo;

    public OrderResponse getOrderById(Long id) {
        return orderRepo.findById(id).map(OrderResponse::fromOrder)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }

    public List<OrderResponse> getOrdersByBusinessOwner(Long id) {
        return orderRepo.findByBusinessOwnerId(id).stream()
                .filter(o -> !Boolean.TRUE.equals(o.getIsDeleted()))
                .map(OrderResponse::fromOrder)
                .toList();
    }

    public List<OrderResponse> getAll() {
        return orderRepo.findByIsDeletedFalse().stream()
                .map(OrderResponse::fromOrder)
                .toList();
    }

    public OrderResponse getOrderByIdAndBusinessOwner(Long orderId, Long businessOwnerId) {
        return orderRepo.findById(orderId)
                .filter(order -> order.getBusinessOwner().getId().equals(businessOwnerId))
                .filter(order -> !Boolean.TRUE.equals(order.getIsDeleted()))
                .map(OrderResponse::fromOrder)
                .orElseThrow(() -> new EntityNotFoundException("Order not found or access denied"));
    }

    public OrderResponse updateOrderStatus(Long orderId, Order.Status status) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        
        order.setStatus(status);
        Order updatedOrder = orderRepo.save(order);
        return OrderResponse.fromOrder(updatedOrder);
    }
}