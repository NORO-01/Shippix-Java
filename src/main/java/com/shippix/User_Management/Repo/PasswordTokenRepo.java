package com.shippix.User_Management.Repo;

import com.shippix.User_Management.Model.PasswordToken;
import com.shippix.User_Management.Model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordTokenRepo extends JpaRepository<PasswordToken,Long> {

    Optional<PasswordToken> findByOtpAndUser(Integer otp, Users user);
    Optional<PasswordToken> findByToken(String token);
    void deleteByUser(Users user);

}
