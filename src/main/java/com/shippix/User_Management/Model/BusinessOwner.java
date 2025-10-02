package com.shippix.User_Management.Model;

import jakarta.persistence.*;
import lombok.*;


@Data
@NoArgsConstructor
@Entity
@Table(name = "business_owners")
@PrimaryKeyJoinColumn(name = "id")
public class BusinessOwner extends Users {

    private String nationalId;
    private String businessName;

    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;
}

