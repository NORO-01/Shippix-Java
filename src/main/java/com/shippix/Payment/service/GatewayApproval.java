package com.shippix.Payment.service;

import com.shippix.Payment.dto.PaymentResponse;
import com.shippix.Payment.enums.PaymentStatus;
import com.shippix.Payment.model.Payment;
import com.shippix.Payment.repo.PaymentRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GatewayApproval
{
    private final PaymentRepo paymentRepo;
    private final ModelMapper modelMapper;

    @Transactional
    public PaymentResponse simulatePayment(Long reqId, boolean success)
    {
        Payment payment = paymentRepo.findByOrderReqId(reqId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (success)
        {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId("TX-" + System.currentTimeMillis());
        }
        else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        Payment updated = paymentRepo.save(payment);
        return modelMapper.map(updated, PaymentResponse.class);
    }
}