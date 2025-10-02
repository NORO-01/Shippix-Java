package com.shippix.Payment.model;
import com.shippix.Order.Model.Order;
import com.shippix.Order.Model.OrderRequest;
import com.shippix.Payment.enums.PaymentMethod;
import com.shippix.Payment.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    //like "FWR-839201"
    @Column(unique = true, nullable = false)
    private String code;

    private Double amount; //the amount the gateway should receive

    @ManyToOne
    @JoinColumn(name = "req_id")
    private OrderRequest orderRequest;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order; // set after success

    private String transactionId; // optional - for real gateways

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}