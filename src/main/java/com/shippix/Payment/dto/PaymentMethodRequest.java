package com.shippix.Payment.dto;

import com.shippix.Payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentMethodRequest
{
    @NotNull(message = "Payment method is required")
    private PaymentMethod method;
}
