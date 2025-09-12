package com.shippix.User_Management.Controller;

import com.shippix.User_Management.DTO.BORequest;
import com.shippix.User_Management.DTO.BOResponse;
import com.shippix.User_Management.DTO.LoginRequest;
import com.shippix.User_Management.DTO.NewPassRequest;
import com.shippix.User_Management.Service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final BusinessOwnerRequestService businessOwnerRequestService;
    private final PasswordService passwordService;

    // ---------------- LOGIN ----------------
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        // Authenticate and generate JWT access token
        String accessToken = authService.login(request.email(), request.password());

        // Create refresh token
        Long userId = authService.getUserIdByEmail(request.email());
        String refreshToken = refreshTokenService.createRefreshToken(userId).getToken();

        // Return both tokens
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        return ResponseEntity.ok(response);
    }

    // ---------------- REGISTER ----------------
    @PostMapping("/register")
    public ResponseEntity<BOResponse> register(@RequestBody BORequest dto) {
        return ResponseEntity.ok(businessOwnerRequestService.submitRequest(dto));
    }

    // ---------------- SET PASS ----------------
    @PostMapping("/setPassword")
    public ResponseEntity<String> setPassword(@RequestParam String token,
                                              @RequestBody NewPassRequest newPassword) {
        try {
            passwordService.setPassword(token, newPassword);
            return ResponseEntity.ok("Password set successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }


    // ---------------- REFRESH TOKEN ----------------
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> payload) {
        String requestToken = payload.get("refreshToken");
        if (requestToken == null || requestToken.isBlank()) {
            return ResponseEntity.badRequest().body("Refresh token is required.");
        }

        try {
            // Validate and get the refresh token
            var refreshToken = refreshTokenService.verifyExpiration(requestToken);

            // Generate new access token
            String newAccessToken = jwtService.generateToken(refreshToken.getUser());

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("refreshToken", refreshToken.getToken()); // reuse existing refresh token
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ---------------- LOGOUT ----------------
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestBody Map<String, String> payload) {
        String requestToken = payload.get("refreshToken");
        if (requestToken == null || requestToken.isBlank()) {
            return ResponseEntity.badRequest().body("Refresh token is required.");
        }

        try {
            refreshTokenService.deleteRefreshToken(requestToken);
            return ResponseEntity.ok("Logged out successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

