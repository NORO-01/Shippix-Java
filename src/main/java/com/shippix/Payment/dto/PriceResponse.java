package com.shippix.Payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceResponse
{
    private Long orderRequestId;
    private Double calculatedPrice;
    private String currency = "EGP";
}