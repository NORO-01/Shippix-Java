package com.shippix.User_Management.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PasswordToken {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer otp;

    private String token;

    @Column(nullable = false)
    private Date expiryTime;

    @OneToOne
    @JoinColumn(name = "user_id",unique = true)
    private Users user;

    @PrePersist
    @PreUpdate
    private void validate() {
        boolean hasOtp = otp != null;
        boolean hasToken = token != null;

        if (hasOtp == hasToken) {
            throw new IllegalStateException("Exactly one of OTP or Token must be set.");
        }
        
        if (!hasOtp && !hasToken) {
            throw new IllegalStateException("Either OTP or Token must be provided.");
        }
    }
}

