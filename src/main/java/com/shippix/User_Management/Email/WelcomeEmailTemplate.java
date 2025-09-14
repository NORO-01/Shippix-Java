package com.shippix.User_Management.Email;

public class WelcomeEmailTemplate implements EmailTemplate {
    private final String email;
    private final String name;

    public WelcomeEmailTemplate(String email, String name) {
        this.email = email;
        this.name = name;
    }

    @Override
    public String getTo() {
        return email;
    }

    @Override
    public String getSubject() {
        return "Welcome to Shippix - Registration Received";
    }

    @Override
    public String getBody() {
        return "Dear " + name + ",<br><br>" +
                "Thank you for registering with Shippix! We have received your business owner request.<br><br>" +
                "Your application is currently under review. You will receive an email notification once your request has been processed.<br><br>" +
                "If you have any questions, please don't hesitate to contact our support team.<br><br>" +
                "Best regards,<br>" +
                "The Shippix Team";
    }

    @Override
    public String getLink() {
        return null; // No link for welcome email
    }
}