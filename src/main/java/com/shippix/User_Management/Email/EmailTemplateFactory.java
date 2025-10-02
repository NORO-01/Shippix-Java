package com.shippix.User_Management.Email;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplateFactory {

    public EmailTemplate createWelcomeEmail(String email, String name) {
        return new WelcomeEmailTemplate(email, name);
    }

    public EmailTemplate createApprovalEmail(String email, String businessName) {
        return new ApprovalEmailTemplate(email, businessName);
    }

    public EmailTemplate createRejectionEmail(String email, String businessName) {
        return new RejectionEmailTemplate(email, businessName);
    }

    public EmailTemplate createOtpEmail(String email, Integer otp) {
        return new OtpEmailTemplate(email, otp);
    }


    public EmailTemplate createPaymentCodeEmail(String email, String code) {
        return new PaymentCodeTemplate(email, code);
    }

}
