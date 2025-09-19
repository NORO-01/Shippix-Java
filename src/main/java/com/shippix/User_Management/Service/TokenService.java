package com.shippix.User_Management.Service;

import com.shippix.User_Management.DTO.UserDTO;
import com.shippix.User_Management.Model.Users;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtService jwtService;
    private final UserService userService;

    public UserDTO extractUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        // Validate & decode token
        String email = jwtService.extractUsername(token);

        // Validate token is not expired
        if (jwtService.getExpiration(token).before(new java.util.Date())) {
            throw new RuntimeException("Token has expired");
        }

        // Get user from database
        Users user = userService.getByEmail(email);

        // Map to DTO
        return UserDTO.fromUser(user);
    }
}