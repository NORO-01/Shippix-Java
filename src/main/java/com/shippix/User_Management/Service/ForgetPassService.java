package com.shippix.User_Management.Service;

import com.shippix.User_Management.DTO.EmailBody;
import com.shippix.User_Management.Model.PasswordToken;
import com.shippix.User_Management.Model.Users;
import com.shippix.User_Management.Repo.PasswordTokenRepo;
import com.shippix.User_Management.Repo.UserRepo;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ForgetPassService {

    private final UserRepo userRepo;
    private final PasswordTokenRepo tokenRepo;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public void sendOtp(String email) throws MessagingException {
        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Delete any existing OTP tokens for this user
        tokenRepo.deleteByUser(user);

        Integer otp = generateOtp();
        PasswordToken token = PasswordToken.builder()
                .otp(otp)
                .expiryTime(new Date(System.currentTimeMillis() + 70 * 1000))
                .user(user)
                .build();

        EmailBody emailBody = new EmailBody(email, "OTP for Reset", "Your OTP: " + otp, null);

        emailService.sendEmail(emailBody);
        tokenRepo.save(token);
    }

    public boolean verifyOtp(String email, Integer otp) {
        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        PasswordToken token = tokenRepo.findByOtpAndUser(otp, user)
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (token.getExpiryTime().before(Date.from(Instant.now()))) {
            tokenRepo.deleteById(token.getId());
            throw new RuntimeException("OTP expired");
        }

        return true;
    }

    @Transactional
    public void changePassword(String email, String newPassword) {
        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }

    private Integer generateOtp() {
        return 100_000 + new Random().nextInt(900_000);
    }
}

