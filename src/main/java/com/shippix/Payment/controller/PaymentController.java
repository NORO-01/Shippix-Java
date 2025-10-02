package com.shippix.Payment.controller;

import com.shippix.Payment.dto.PaymentMethodRequest;
import com.shippix.Payment.dto.PaymentResponse;
import com.shippix.Payment.service.CreatePayment;
import com.shippix.Payment.service.GatewayApproval;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController
{
    private final CreatePayment createPayment;
    private final GatewayApproval gatewayApproval;

    public PaymentController(CreatePayment createPayment, GatewayApproval gatewayApproval)
    {
        this.createPayment = createPayment;
        this.gatewayApproval = gatewayApproval;
    }

    @PostMapping("/create/{reqId}")
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable Long reqId,
            @Valid @RequestBody PaymentMethodRequest request
    ) {
        return ResponseEntity.ok(
                createPayment.createPayment(reqId, request.getMethod())
        );
    }

    @PostMapping("/{paymentId}/validate")
    public ResponseEntity<PaymentResponse> validatePayment(
            @PathVariable Long paymentId,
            @RequestParam String code
    )
    {
        return ResponseEntity.ok(gatewayApproval.validatePayment(paymentId, code));
    }
}