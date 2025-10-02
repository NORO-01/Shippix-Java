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
public class GatewayApproval {

    private final PaymentRepo paymentRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public PaymentResponse validatePayment(Long paymentId, String code)
    {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getCode().equals(code))
        {
            payment.setStatus(PaymentStatus.SUCCESS);
        }
        else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        Payment updated = paymentRepository.save(payment);
        return modelMapper.map(updated, PaymentResponse.class);
    }
}
