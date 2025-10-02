package com.shippix.User_Management.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPassRequest(
        @NotBlank(message = "Password must not be blank")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = ".*[0-9].*", message = "Password must contain at least one numeric digit")
        @Pattern(regexp = ".*[!@#$%^&*(),.?\":{}|<>].*", message = "Password must contain at least one special character")
        String oldPassword,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = ".*[0-9].*", message = "Password must contain at least one numeric digit")
        @Pattern(regexp = ".*[!@#$%^&*(),.?\":{}|<>].*", message = "Password must contain at least one special character")
        String newPassword,

        @NotBlank(message = "Repeat Password must not be blank")
        String repeatPassword
) {}

