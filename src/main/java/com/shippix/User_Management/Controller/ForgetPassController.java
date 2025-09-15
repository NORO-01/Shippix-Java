package com.shippix.User_Management.Controller;

import com.shippix.User_Management.DTO.ForgetPassRequest;
import com.shippix.User_Management.Email.EmailTemplate;
import com.shippix.User_Management.Email.EmailTemplateFactory;
import com.shippix.User_Management.Model.PasswordToken;
import com.shippix.User_Management.Service.EmailService;
import com.shippix.User_Management.Service.ForgetPassService;
import jakarta.validation.Valid;
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
    private final EmailService emailService;
    private final EmailTemplateFactory emailTemplateFactory;

    @PostMapping("/verifyMail/{email}")
    public ResponseEntity<String> verifyEmail(@PathVariable String email) {

        PasswordToken token = forgetPassService.createOtp(email);
        EmailTemplate template = emailTemplateFactory.createOtpEmail(email, token.getOtp());
        emailService.sendEmail(template);

        return ResponseEntity.ok("OTP sent to email");

    }

    @PostMapping("/verifyOtp/{email}/{otp}")
    public ResponseEntity<String> verifyOtp(@PathVariable String email, @PathVariable Integer otp) {
        forgetPassService.verifyOtp(email, otp);
        return ResponseEntity.ok("OTP verified successfully");
    }

    @PostMapping("/changePassword/{email}")
    public ResponseEntity<String> changePassword(@PathVariable String email,
                                                 @Valid @RequestBody ForgetPassRequest newPassword) {
        if (!Objects.equals(newPassword.password(), newPassword.repeatPassword())) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body("Passwords do not match");
        }
        forgetPassService.changePassword(email, newPassword.password());
        return ResponseEntity.ok("Password changed successfully");
    }
}


