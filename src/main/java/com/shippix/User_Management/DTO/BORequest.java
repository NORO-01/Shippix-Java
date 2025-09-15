package com.shippix.User_Management.DTO;

import com.shippix.User_Management.Model.BusinessType;
import jakarta.validation.constraints.*;

public record BORequest(

        @NotBlank(message = "Business name must not be blank")
        @Pattern(regexp = "^[a-zA-Z0-9\\s]+$", message = "Business name special characters are not allowed")
        @Pattern(regexp = "^\\S.*", message = "Business name first character cannot be space")
        String businessName,

        @NotBlank(message = "Owner name must not be blank")
        @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Owner name must contain only letters and spaces")
        @Pattern(regexp = "^\\S.*", message = "Owner name first character cannot be space")
        String name,

        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be in valid format")
        @Pattern(regexp = "^\\S.*", message = "Email first character cannot be space")
        String email,

        @NotBlank(message = "National ID must not be blank")
        @Pattern(regexp = "^[0-9]{14}$", message = "National ID must be exactly 14 digits")
        String nationalId,

        @NotBlank(message = "Phone number must not be blank")
        @Pattern(regexp = "^[0-9]+$", message = "Phone number must contain only digits")
        @Pattern(regexp = "^\\S.*", message = "Phone number first character cannot be space")
        String phoneNumber,

        @NotNull(message = "Latitude must not be null")
        @DecimalMin(value = "-90.0", inclusive = true, message = "Latitude must be >= -90")
        @DecimalMax(value = "90.0", inclusive = true, message = "Latitude must be <= 90")
        Double latitude,

        @NotNull(message = "Longitude must not be null")
        @DecimalMin(value = "-180.0", inclusive = true, message = "Longitude must be >= -180")
        @DecimalMax(value = "180.0", inclusive = true, message = "Longitude must be <= 180")
        Double longitude,

        @NotNull(message = "Business type must not be null")
        BusinessType businessType,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = ".*[0-9].*", message = "Password must contain at least one numeric digit")
        @Pattern(regexp = ".*[!@#$%^&*(),.?\":{}|<>].*", message = "Password must contain at least one special character")
        String password,

        @NotBlank(message = "Confirm password must not be blank")
        String confirmPassword
) {
        @AssertTrue(message = "Password and confirm password must match")
        public boolean isPasswordMatching() {
                return password != null && password.equals(confirmPassword);
        }
}










