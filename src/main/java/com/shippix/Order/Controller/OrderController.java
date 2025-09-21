package com.shippix.Order.Controller;

import com.shippix.Order.DTO.OrderResponse;
import com.shippix.Order.Service.OrderService;
import com.shippix.User_Management.Model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long businessOwnerId = userPrincipal.getId();
        
        List<OrderResponse> orders = orderService.getOrdersByBusinessOwner(businessOwnerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id,
            Authentication authentication) {
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long businessOwnerId = userPrincipal.getId();
        
        OrderResponse order = orderService.getOrderByIdAndBusinessOwner(id, businessOwnerId);
        return ResponseEntity.ok(order);
    }
}
