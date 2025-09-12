package com.shippix.User_Management.Service;

import com.shippix.User_Management.Model.UserPrincipal;
import com.shippix.User_Management.Model.Users;
import com.shippix.User_Management.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepo userRepo;

    public String login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Users user = userRepo.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return jwtService.generateToken(user);
    }

    public Long getUserIdByEmail(String email) {
        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

}