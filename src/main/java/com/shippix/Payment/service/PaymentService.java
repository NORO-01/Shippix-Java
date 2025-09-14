package com.shippix.Payment.service;
import com.shippix.Payment.dto.PaymentRequest;
import com.shippix.Payment.dto.PaymentResponse;
import com.shippix.Payment.enums.PaymentStatus;
import com.shippix.Payment.model.Payment;
import com.shippix.Payment.repo.PaymentRepo;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentService
{
    private final PaymentRepo paymentRepository;
    private final ModelMapper modelMapper;

    public PaymentService(PaymentRepo paymentRepository, ModelMapper modelMapper) {
        this.paymentRepository = paymentRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request)
    {
        //make sure order not paid before
        paymentRepository.findByOrderId(request.getOrderId())
                .ifPresent(p -> { throw new RuntimeException("Order already paid!"); });

        //calculation
        BigDecimal calculatedPrice = request.getBasePrice().add
                (
                request.getWeightFactor().multiply(request.getDistanceFactor())
                );


        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setBasePrice(request.getBasePrice());
        payment.setWeightFactor(request.getWeightFactor());
        payment.setDistanceFactor(request.getDistanceFactor());
        payment.setCalculatedPrice(calculatedPrice);
        payment.setMethod(request.getMethod());
        payment.setStatus(PaymentStatus.PENDING);

        Payment saved = paymentRepository.save(payment);

        return modelMapper.map(saved, PaymentResponse.class);
    }


    @Transactional
    public PaymentResponse simulatePayment(String orderId, boolean success)
    {
    Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));

    if (success)
    {
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setReferenceCode("REF-" + System.currentTimeMillis());
    }
    else {
        payment.setStatus(PaymentStatus.FAILED);
    }

    Payment updated = paymentRepository.save(payment);
    return modelMapper.map(updated, PaymentResponse.class);
    }


    public PaymentResponse getPayment(String orderId)
    {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return modelMapper.map(payment, PaymentResponse.class);
    }

    public List<PaymentResponse> getAllPayments()
    {
        return paymentRepository.findAll()
            .stream()
            .map(p -> modelMapper.map(p, PaymentResponse.class))
            .toList();
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
}