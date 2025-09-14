package com.shippix.User_Management.DTO;

import com.shippix.User_Management.Model.BusinessOwnerRequest;
import com.shippix.User_Management.Model.BusinessType;

public record BOResponse(
        Long id,
        String name,
        String email,
        String businessName,
        BusinessType businessType,
        BusinessOwnerRequest.Status status
) {}