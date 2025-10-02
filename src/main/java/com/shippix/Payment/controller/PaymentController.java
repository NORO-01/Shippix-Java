package com.shippix.Payment.controller;

import com.shippix.Payment.dto.PaymentRequest;
import com.shippix.Payment.dto.PaymentResponse;
import com.shippix.Payment.enums.PaymentMethod;
import com.shippix.Payment.service.BusinessOwnerPayment;
import com.shippix.Payment.service.GatewayApproval;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
public class PaymentController
{
    private final BusinessOwnerPayment businessOwnerPayment;
    private final GatewayApproval gatewayApproval;

    public PaymentController(BusinessOwnerPayment businessOwnerPayment, GatewayApproval gatewayApproval) {
        this.businessOwnerPayment = businessOwnerPayment;
        this.gatewayApproval = gatewayApproval;
    }

    @PostMapping("/create/{reqId}")
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable Long reqId,
            @RequestParam PaymentMethod method)
    {
        return ResponseEntity.ok(businessOwnerPayment.createPayment(reqId, method));
    }

    @PutMapping("/{reqId}/simulate")
    public ResponseEntity<PaymentResponse> simulatePayment(
            @PathVariable Long reqId,
            @RequestParam boolean success) {
        return ResponseEntity.ok(gatewayApproval.simulatePayment(reqId, success));
    }

    // cancel
//    @PutMapping("/{orderId}/cancel")
//    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable Long orderId) {
//        return ResponseEntity.ok(paymentService.cancelPayment(orderId));
//    }
}
