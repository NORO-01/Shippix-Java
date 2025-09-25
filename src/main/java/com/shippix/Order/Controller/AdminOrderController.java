package com.shippix.Order.Controller;

import com.shippix.Order.DTO.OrderRequestResponse;
import com.shippix.Order.DTO.OrderResponse;
import com.shippix.Order.DTO.OrderStatusUpdateRequest;
import com.shippix.Order.Model.OrderRequest;
import com.shippix.Order.Service.OrderRequestService;
import com.shippix.Order.Service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderRequestService orderRequestService;
    private final OrderService orderService;

    // Order Request Management
    @GetMapping("/order-requests")
    public ResponseEntity<List<OrderRequestResponse>> getAllOrderRequests() {
        List<OrderRequestResponse> requests = orderRequestService.getAllRequests()
                .stream()
                .map(OrderRequestResponse::fromOrderRequest)
                .toList();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/order-requests/{id}")
    public ResponseEntity<OrderRequestResponse> getOrderRequestById(@PathVariable Long id) {
        OrderRequest orderRequest = orderRequestService.getRequestById(id);
        return ResponseEntity.ok(OrderRequestResponse.fromOrderRequest(orderRequest));
    }

    @PutMapping("/order-requests/{id}/approve")
    public ResponseEntity<OrderResponse> approveOrderRequest(@PathVariable Long id) {
        OrderResponse order = orderRequestService.approveRequest(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/order-requests/{id}/reject")
    public ResponseEntity<OrderRequestResponse> rejectOrderRequest(@PathVariable Long id) {
        OrderRequest orderRequest = orderRequestService.rejectRequest(id);
        return ResponseEntity.ok(OrderRequestResponse.fromOrderRequest(orderRequest));
    }

    // Order Management
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAll();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        OrderResponse order = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(order);
    }
}
