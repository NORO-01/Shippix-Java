package com.shippix.Payment.dto;

import com.shippix.Payment.enums.PaymentMethod;
import com.shippix.Payment.enums.PaymentStatus;

import java.time.LocalDateTime;

public class PaymentResponse
{
    private Long id;
    private Long orderRequestId;
    private Double amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String code;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderRequestId() { return orderRequestId; }
    public void setOrderRequestId(Long orderRequestId) { this.orderRequestId = orderRequestId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
