package com.shippix.User_Management.Email;

public class ApprovalEmailTemplate implements EmailTemplate {
    private final String email;
    private final String businessName;

    public ApprovalEmailTemplate(String email, String businessName) {
        this.email = email;
        this.businessName = businessName;
    }

    @Override
    public String getTo() {
        return email;
    }

    @Override
    public String getSubject() {
        return "Congratulations! Your Shippix Account Has Been Approved";
    }

    @Override
    public String getBody() {
        return "Dear Business Owner,<br><br>" +
                "Great news! Your business owner account for <strong>" + businessName + "</strong> has been approved!<br><br>" +
                "You can now log in to your Shippix account using the email and password you provided during registration.<br><br>" +
                "Welcome to the Shippix family! We're excited to have you on board.<br><br>" +
                "If you have any questions or need assistance, please don't hesitate to contact our support team.<br><br>" +
                "Best regards,<br>" +
                "The Shippix Team";
    }

    @Override
    public String getLink() {
        return null; // No link for approval email
    }
}