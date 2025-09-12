package com.shippix.User_Management.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.*;


@Data
@NoArgsConstructor
@Entity
@Table(name = "business_owners")
@PrimaryKeyJoinColumn(name = "id")
public class BusinessOwner extends Users {

    private String nationalId;
    private String businessName;
    private String businessType;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;
}
