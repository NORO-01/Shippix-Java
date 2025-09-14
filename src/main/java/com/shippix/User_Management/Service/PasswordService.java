package com.shippix.User_Management.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordService {
//
//    private final PasswordTokenRepo passwordTokenRepo;
//    private final UserRepo userRepo;
//    private final BCryptPasswordEncoder passwordEncoder;
//
//    @Transactional
//    public void setPassword(String token, NewPassRequest newPassword) {
//        PasswordToken pt = passwordTokenRepo.findByToken(token)
//                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));
//
//        if (pt.getExpiryTime().before(new Date())) {
//            passwordTokenRepo.delete(pt);
//            throw new RuntimeException("Token expired");
//        }
//
//        if (!Objects.equals(newPassword.password(), newPassword.repeatPassword())) {
//            throw new RuntimeException("Passwords do not match");
//        }
//
//        Users user = pt.getUser();
//        user.setPassword(passwordEncoder.encode(newPassword.password()));
//        userRepo.save(user);
//
//        passwordTokenRepo.delete(pt);
//    }
}

