package com.shippix.User_Management.Email;

public class PaymentCodeTemplate implements EmailTemplate
{
    private final String to;
    private final String code;

    public PaymentCodeTemplate(String to, String code)
    {
        this.to = to;
        this.code = code;
    }

    @Override
    public String getTo() {
        return to;
    }

    @Override
    public String getSubject() {
        return "Your Payment Verification Code";
    }

    @Override
    public String getBody()
    {
        return "Dear user,<br><br>" +
                "Your payment verification code is: <b>" + code + "</b><br>" +
                "Please enter this code in the app to confirm your payment.";
    }

    @Override
    public String getLink() {
        return null;
    }
}