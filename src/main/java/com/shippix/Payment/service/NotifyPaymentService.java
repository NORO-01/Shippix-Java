package com.shippix.Payment.service;

import com.shippix.Payment.model.Payment;
import com.shippix.User_Management.Service.EmailService;
import com.shippix.User_Management.Email.PaymentCodeTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotifyPaymentService
{
    private final EmailService emailService;

    public void sendPaymentCode(Payment payment)
    {
        if (payment.getOrderRequest() == null || payment.getOrderRequest().getCustEmail() == null)
        {
            throw new RuntimeException("Business owner email not available for this payment!");
        }

        String BOEmail = payment.getOrderRequest().getBusinessOwner().getEmail();
        String code = payment.getCode();

        PaymentCodeTemplate template = new PaymentCodeTemplate(BOEmail, code);
        emailService.sendEmail(template);
    }
}
