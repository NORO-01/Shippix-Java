package com.shippix.Order.Controller;

import com.shippix.Order.DTO.OrderFeedbackRequest;
import com.shippix.Order.DTO.OrderFeedbackResponse;
import com.shippix.Order.Service.OrderFeedbackService;
import com.shippix.User_Management.Model.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderFeedbackController {

    private final OrderFeedbackService orderFeedbackService;

    @PostMapping("/{orderId}/feedback")
    public ResponseEntity<OrderFeedbackResponse> submitFeedback(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderFeedbackRequest request,
            Authentication authentication) {
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long businessOwnerId = userPrincipal.getId();
        
        OrderFeedbackResponse feedback = orderFeedbackService.submitFeedback(orderId, businessOwnerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(feedback);
    }

    @GetMapping("/{orderId}/feedback")
    public ResponseEntity<OrderFeedbackResponse> getFeedback(
            @PathVariable Long orderId,
            Authentication authentication) {
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long businessOwnerId = userPrincipal.getId();
        
        OrderFeedbackResponse feedback = orderFeedbackService.getFeedbackByOrderIdAndBusinessOwner(orderId, businessOwnerId);
        return ResponseEntity.ok(feedback);
    }
}
