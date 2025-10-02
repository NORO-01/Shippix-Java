package com.shippix.Payment.service;
import com.shippix.Order.Model.OrderRequest;
import com.shippix.Payment.dto.PaymentRequest;
import com.shippix.Payment.dto.PaymentResponse;
import com.shippix.Payment.enums.PaymentMethod;
import com.shippix.Payment.enums.PaymentStatus;
import com.shippix.Payment.model.Payment;
import com.shippix.Payment.repo.PaymentRepo;
import com.shippix.Order.Repo.OrderRequestRepo;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessOwnerPayment
{
    private final PaymentRepo paymentRepo;
    private final ModelMapper modelMapper;
    private final PricingService pricingService;
    private final OrderRequestRepo orderRequestRepo;

    @Transactional
    public PaymentResponse createPayment(Long reqId, PaymentMethod method)
    {
        OrderRequest orderRequest = orderRequestRepo.findById(reqId)
                .orElseThrow(() -> new RuntimeException("OrderRequest not found"));

        //ensure not paid before
        paymentRepo.findByOrderReqId(reqId)
                .ifPresent(p -> { throw new RuntimeException("Request already has a payment!"); });

        double price = pricingService.calculatePrice(orderRequest);

        Payment payment = new Payment();
        payment.setOrderRequest(orderRequest);
        payment.setAmount(price);
        payment.setMethod(method);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCode("PAY-" + System.currentTimeMillis());

        Payment saved = paymentRepo.save(payment);
        return modelMapper.map(saved, PaymentResponse.class);
    }
}



    //TBD -> REFUND
//    @Transactional
//    public PaymentResponse cancelPayment(Long orderId) {
//        Payment payment = paymentRepository.findByOrderId(orderId)
//                .orElseThrow(() -> new RuntimeException("Payment not found"));
//
//        if (payment.getStatus() == PaymentStatus.PENDING) {
//            payment.setStatus(PaymentStatus.CANCELLED);
//        } else {
//            throw new RuntimeException("Only pending payments can be cancelled!");
//        }
//
//        Payment updated = paymentRepository.save(payment);
//        return modelMapper.map(updated, PaymentResponse.class);
//    }
