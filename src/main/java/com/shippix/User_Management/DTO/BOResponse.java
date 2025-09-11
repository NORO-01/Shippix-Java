package com.shippix.User_Management.DTO;

import com.shippix.User_Management.Model.BusinessOwnerRequest;

public record BOResponse(
        Long id,
        String name,
        String email,
        String businessName,
        String businessType,
        BusinessOwnerRequest.Status status
) {}
