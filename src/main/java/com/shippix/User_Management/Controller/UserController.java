package com.shippix.User_Management.Controller;

import com.shippix.User_Management.DTO.PassChangeRequest;
import com.shippix.User_Management.Model.UserPrincipal;
import com.shippix.User_Management.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/changePassword")
    public ResponseEntity<String> changePasswordAuthenticated(@RequestBody PassChangeRequest request,
                                                              @AuthenticationPrincipal UserPrincipal currentUser) {
        if (!Objects.equals(request.newPassword(), request.repeatPassword())) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body("Passwords do not match");
        }

        userService.changePassword(currentUser.getEmail(), request.newPassword(), request.oldPassword());
        return ResponseEntity.ok("Password changed successfully!");
    }
}
