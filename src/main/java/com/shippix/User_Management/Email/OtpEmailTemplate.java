package com.shippix.User_Management.Email;

public class OtpEmailTemplate implements EmailTemplate {
    private final String email;
    private final Integer otp;

    public OtpEmailTemplate(String email, Integer otp) {
        this.email = email;
        this.otp = otp;
    }

    @Override
    public String getTo() {
        return email;
    }

    @Override
    public String getSubject() {
        return "OTP for Password Reset";
    }

    @Override
    public String getBody() {
        return "Dear User,<br><br>" +
                "Here is your One-Time Password (OTP) for resetting your account password:<br><br>" +
                "<h2>" + otp + "</h2><br>" +
                "This OTP is valid for 70 seconds. Please do not share it with anyone.<br><br>" +
                "If you did not request a password reset, please ignore this email.<br><br>" +
                "Best regards,<br>" +
                "The Shippix Team";
    }

    @Override
    public String getLink() {
        return null;
    }
}
