package com.shippix.User_Management.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "business_owner_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessOwnerRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phoneNumber;
    private String nationalId;
    private String businessName;

    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }
}