package com.shippix.Order.Controller;

import com.shippix.Order.DTO.OrderCreateRequest;
import com.shippix.Order.DTO.OrderRequestResponse;
import com.shippix.Order.Service.OrderRequestService;
import com.shippix.User_Management.Model.BusinessOwner;
import com.shippix.User_Management.Model.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-requests")
@RequiredArgsConstructor
public class OrderRequestController {

    private final OrderRequestService orderRequestService;

    @PostMapping
    public ResponseEntity<OrderRequestResponse> submitRequest(
            @Valid @RequestBody OrderCreateRequest request,
            Authentication authentication) {
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        BusinessOwner businessOwner = (BusinessOwner) userPrincipal.getUsers();
        
        var orderRequest = orderRequestService.submitRequest(businessOwner, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OrderRequestResponse.fromOrderRequest(orderRequest));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<OrderRequestResponse>> getMyRequests(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long businessOwnerId = userPrincipal.getId();
        
        List<OrderRequestResponse> requests = orderRequestService.listByBusinessOwner(businessOwnerId)
                .stream()
                .map(OrderRequestResponse::fromOrderRequest)
                .toList();
        
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderRequestResponse> getRequestById(
            @PathVariable Long id,
            Authentication authentication) {
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long businessOwnerId = userPrincipal.getId();
        
        var orderRequest = orderRequestService.getRequestByIdAndBusinessOwner(id, businessOwnerId);
        return ResponseEntity.ok(OrderRequestResponse.fromOrderRequest(orderRequest));
    }
}
