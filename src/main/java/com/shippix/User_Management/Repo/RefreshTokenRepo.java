package com.shippix.User_Management.Repo;

import com.shippix.User_Management.Model.RefreshToken;
import com.shippix.User_Management.Model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(Users user);
}