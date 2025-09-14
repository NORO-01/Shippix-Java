package com.shippix.User_Management.Email;

public interface EmailTemplate {
    String getTo();
    String getSubject();
    String getBody();
    String getLink();
}