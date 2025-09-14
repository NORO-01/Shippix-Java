package com.shippix.User_Management.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Integer otp;

    @Column(nullable = false)
    private Date expiryTime;

    private boolean verified = false;

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private Users user;
}


