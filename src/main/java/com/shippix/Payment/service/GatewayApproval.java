//package com.shippix.Payment.service;
//
//import com.shippix.Payment.dto.PaymentResponse;
//import com.shippix.Payment.enums.PaymentStatus;
//import com.shippix.Payment.model.Payment;
//import com.shippix.Payment.repo.PaymentRepo;
//import org.modelmapper.ModelMapper;
//import org.springframework.transaction.annotation.Transactional;
//
//public class GatewayApproval {
//    private final PaymentRepo paymentRepository;
//    private final ModelMapper modelMapper;
//
//    public GatewayApproval(PaymentRepo paymentRepository, ModelMapper modelMapper) {
//        this.paymentRepository = paymentRepository;
//        this.modelMapper = modelMapper;
//    }
//    @Transactional
//    public PaymentResponse simulatePayment(String orderId, boolean success)
//    {
//        Payment payment = paymentRepository.findByOrderId(orderId)
//                .orElseThrow(() -> new RuntimeException("Payment not found"));
//
//        if (success)
//        {
//            payment.setStatus(PaymentStatus.SUCCESS);
//            payment.setReferenceCode("REF-" + System.currentTimeMillis());
//        }
//        else {
//            payment.setStatus(PaymentStatus.FAILED);
//        }
//
//        Payment updated = paymentRepository.save(payment);
//        return modelMapper.map(updated, PaymentResponse.class);
//    }
//}
