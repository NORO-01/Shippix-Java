package com.shippix.User_Management.DTO;

public record BORequest(
        String name,
        String email,
        String phoneNumber,
        String nationalId,
        String businessName,
        String businessType,
        Double latitude,
        Double longitude
) {}