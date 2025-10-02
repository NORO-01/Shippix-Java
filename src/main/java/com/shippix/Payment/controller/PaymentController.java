//package com.shippix.Payment.controller;
//
//import com.shippix.Payment.dto.PaymentRequest;
//import com.shippix.Payment.dto.PaymentResponse;
//import com.shippix.Payment.service.BusinessOwnerPayment;
//import com.shippix.Payment.service.GatewayApproval;
//import jakarta.validation.Valid;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/payments")
//public class PaymentController
//{
//    private final BusinessOwnerPayment businessOwnerPayment;
//    private final GatewayApproval gatewayApproval;
//
//    public PaymentController(BusinessOwnerPayment businessOwnerPayment, GatewayApproval gatewayApproval) {
//        this.businessOwnerPayment = businessOwnerPayment;
//        this.gatewayApproval = gatewayApproval;
//    }
//
//    @PostMapping("/create")
//    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
//        return ResponseEntity.ok(businessOwnerPayment.createPayment(request));
//    }
//
//    @PutMapping("/{orderId}/simulate")
//    public ResponseEntity<PaymentResponse> simulatePayment(
//            @PathVariable String orderId,
//            @RequestParam boolean success
//    )
//    {
//        return ResponseEntity.ok(gatewayApproval.simulatePayment(orderId, success));
//    }
//
//
//    @GetMapping("/{orderId}")
//    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String orderId) {
//        return ResponseEntity.ok(businessOwnerPayment.getPayment(orderId));
//    }
//
//    @GetMapping
//    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
//        return ResponseEntity.ok(businessOwnerPayment.getAllPayments());
//    }
//
//    // cancel
////    @PutMapping("/{orderId}/cancel")
////    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable Long orderId) {
////        return ResponseEntity.ok(paymentService.cancelPayment(orderId));
////    }
//}
