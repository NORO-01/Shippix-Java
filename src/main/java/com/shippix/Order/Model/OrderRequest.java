package com.shippix.Order.Model;

import com.shippix.User_Management.Model.BusinessOwner;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_req")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "req_id")
    private Long reqId;

    @ManyToOne
    @JoinColumn(name = "business_owner_id", nullable = false)
    private BusinessOwner businessOwner;

    @Column(name = "order_description", columnDefinition = "TEXT")
    private String orderDescription;

    @Column(name = "notes_to_driver")
    private String notesToDriver;

    @Column(name = "package_weight")
    private Double packageWeight;

    @Column(name = "package_value")
    private Double packageValue;

    @Column(name = "delivery_distance")
    private Double deliveryDistance;

//    @Column(name = "delivery_address")
//    private String deliveryAddress;

    // Location coordinates
    @Column(name = "from_latitude")
    private Double fromLatitude;

    @Column(name = "from_longitude")
    private Double fromLongitude;

    @Column(name = "to_latitude")
    private Double toLatitude;

    @Column(name = "to_longitude")
    private Double toLongitude;

    @Column(name = "cust_name")
    private String custName;

    @Column(name = "cust_phone_number")
    private String custPhoneNumber;

    @Column(name = "cust_email")
    private String custEmail;

    @Enumerated(EnumType.STRING)
    private Status decision;

    @UpdateTimestamp
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}