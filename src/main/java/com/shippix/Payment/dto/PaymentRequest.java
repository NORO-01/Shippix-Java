package com.shippix.Payment.dto;
import com.shippix.Payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class  PaymentRequest
{
    @NotNull(message = "Order ID is required")
    private String orderId;

    @NotNull(message = "Base price is required")
    private BigDecimal basePrice;

    @NotNull(message = "Weight factor is required")
    private BigDecimal weightFactor;

    @NotNull(message = "Distance factor is required")
    private BigDecimal distanceFactor;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getWeightFactor() {
        return weightFactor;
    }

    public void setWeightFactor(BigDecimal weightFactor) {
        this.weightFactor = weightFactor;
    }

    public BigDecimal getDistanceFactor() {
        return distanceFactor;
    }

    public void setDistanceFactor(BigDecimal distanceFactor) {
        this.distanceFactor = distanceFactor;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }
}
