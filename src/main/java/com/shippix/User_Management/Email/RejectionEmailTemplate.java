package com.shippix.User_Management.Email;

public class RejectionEmailTemplate implements EmailTemplate {
    private final String email;
    private final String businessName;

    public RejectionEmailTemplate(String email, String businessName) {
        this.email = email;
        this.businessName = businessName;
    }

    @Override
    public String getTo() {
        return email;
    }

    @Override
    public String getSubject() {
        return "Business Owner Request Update";
    }

    @Override
    public String getBody() {
        return "Dear Business Owner,<br><br>" +
                "Thank you for your interest in joining Shippix. After careful review, we regret to inform you that your business owner request for <strong>" + businessName + "</strong> could not be approved at this time.<br><br>" +
                "This decision was based on our current business requirements and criteria. We encourage you to reapply in the future as our requirements may change.<br><br>" +
                "If you have any questions about this decision or would like to discuss your application further, please don't hesitate to contact our support team.<br><br>" +
                "Thank you for your understanding.<br><br>" +
                "Best regards,<br>" +
                "The Shippix Team";
    }

    @Override
    public String getLink() {
        return null; // No link for rejection email
    }
}