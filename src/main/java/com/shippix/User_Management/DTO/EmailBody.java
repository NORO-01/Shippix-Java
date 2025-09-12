package com.shippix.User_Management.DTO;

import lombok.Builder;

@Builder
public record EmailBody(String to, String subject, String text, String link) {
}
