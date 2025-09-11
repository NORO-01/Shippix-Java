package com.shippix.User_Management.Controller;

import com.shippix.User_Management.DTO.BORequest;
import com.shippix.User_Management.DTO.BOResponse;
import com.shippix.User_Management.DTO.LoginRequest;
import com.shippix.User_Management.Service.AuthService;
import com.shippix.User_Management.Service.BusinessOwnerRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final BusinessOwnerRequestService businessOwnerRequestService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<BOResponse> register(@RequestBody BORequest dto) {
        return ResponseEntity.ok(businessOwnerRequestService.submitRequest(dto));
    }
}


