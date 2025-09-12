package com.shippix.User_Management.Controller;

import com.shippix.User_Management.DTO.NewPassRequest;
import com.shippix.User_Management.Service.ForgetPassService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;


import java.util.Objects;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ForgetPassController {

    private final ForgetPassService forgetPassService;

    @PostMapping("/verifyMail/{email}")
    public ResponseEntity<String> verifyEmail(@PathVariable String email) {
        try {
            forgetPassService.sendOtp(email);
            return ResponseEntity.ok("OTP sent to email");
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email");
        }
    }

    @PostMapping("/verifyOtp/{email}/{otp}")
    public ResponseEntity<String> verifyOtp(@PathVariable String email, @PathVariable Integer otp) {
        forgetPassService.verifyOtp(email, otp);
        return ResponseEntity.ok("OTP verified successfully");
    }

    @PostMapping("/changePassword/{email}")
    public ResponseEntity<String> changePassword(@PathVariable String email,
                                                 @RequestBody NewPassRequest newPassword) {
        if (!Objects.equals(newPassword.password(), newPassword.repeatPassword())) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body("Passwords do not match");
        }
        forgetPassService.changePassword(email, newPassword.password());
        return ResponseEntity.ok("Password changed successfully");
    }
}


