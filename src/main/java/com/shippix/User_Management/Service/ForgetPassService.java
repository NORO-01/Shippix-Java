package com.shippix.User_Management.Service;

import com.shippix.User_Management.Model.PasswordToken;
import com.shippix.User_Management.Model.Users;
import com.shippix.User_Management.Repo.PasswordTokenRepo;
import com.shippix.User_Management.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ForgetPassService {

    private final UserRepo userRepo;
    private final PasswordTokenRepo tokenRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public PasswordToken createOtp(String email) {
        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Delete existing tokens
        tokenRepo.deleteByUser(user);

        Integer otp = generateOtp();
        PasswordToken token = PasswordToken.builder()
                .otp(otp)
                .expiryTime(new Date(System.currentTimeMillis() + 70 * 1000))
                .user(user)
                .build();

        return tokenRepo.save(token);
    }

    @Transactional
    public void verifyOtp(String email, Integer otp) {
        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        PasswordToken token = tokenRepo.findByOtpAndUser(otp, user)
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (token.getExpiryTime().before(new Date())) {
            tokenRepo.deleteById(token.getId());
            throw new RuntimeException("OTP expired");
        }

        token.setVerified(true);
        tokenRepo.save(token);
    }

    @Transactional
    public void changePassword(String email, String newPassword) {
        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        PasswordToken token = tokenRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("No OTP found"));

        if (!token.isVerified()) {
            throw new RuntimeException("OTP not verified");
        }

        if (token.getExpiryTime().before(new Date())) {
            tokenRepo.deleteById(token.getId());
            throw new RuntimeException("OTP expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        tokenRepo.delete(token);
    }

    private Integer generateOtp() {
        return 100_000 + new Random().nextInt(900_000);
    }
}

