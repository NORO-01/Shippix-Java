package com.shippix.Order.DTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    @NotBlank(message = "Order description must not be blank")
    @Pattern(regexp = "^\\S.*", message = "Order description first character cannot be space")
    private String orderDescription;

    @Pattern(regexp = "^\\S.*", message = "Notes to driver first character cannot be space")
    private String notesToDriver;

    @NotNull(message = "Package weight must not be blank")
    @DecimalMin(value = "0.01", message = "Package weight must be greater than 0")
    private Double packageWeight;

    @NotNull(message = "Package value must not be blank")
    @DecimalMin(value = "0.01", message = "Package value must be greater than 0")
    private Double packageValue;

//    @NotBlank(message = "Delivery address must not be blank")
//    @Pattern(regexp = "^[a-zA-Z0-9\\s,.-]+$", message = "Delivery address special characters are not allowed (except , . -)")
//    @Pattern(regexp = "^\\S.*", message = "Delivery address first character cannot be space")
//    private String deliveryAddress;

    // Location coordinates
    @NotNull(message = "From latitude must not be null")
    @DecimalMin(value = "-90.0", inclusive = true, message = "From latitude must be >= -90")
    @DecimalMax(value = "90.0", inclusive = true, message = "From latitude must be <= 90")
    private Double fromLatitude;

    @NotNull(message = "From longitude must not be null")
    @DecimalMin(value = "-180.0", inclusive = true, message = "From longitude must be >= -180")
    @DecimalMax(value = "180.0", inclusive = true, message = "From longitude must be <= 180")
    private Double fromLongitude;

    @NotNull(message = "To latitude must not be null")
    @DecimalMin(value = "-90.0", inclusive = true, message = "To latitude must be >= -90")
    @DecimalMax(value = "90.0", inclusive = true, message = "To latitude must be <= 90")
    private Double toLatitude;

    @NotNull(message = "To longitude must not be null")
    @DecimalMin(value = "-180.0", inclusive = true, message = "To longitude must be >= -180")
    @DecimalMax(value = "180.0", inclusive = true, message = "To longitude must be <= 180")
    private Double toLongitude;

    @NotBlank(message = "Customer name must not be blank")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Customer name numbers are not allowed")
    @Pattern(regexp = "^\\S.*", message = "Customer name first character cannot be space")
    private String custName;

    @NotBlank(message = "Customer phone number must not be blank")
    @Pattern(regexp = "^[0-9]+$", message = "Customer phone number special characters are not allowed")
    @Pattern(regexp = "^\\S.*", message = "Customer phone number first character cannot be space")
    @Size(min = 11, max = 11, message = "Customer phone number must be exactly 11 digits")
    private String custPhoneNumber;

    @NotBlank(message = "Customer email must not be blank")
    @Email(message = "Customer email must be in valid format")
    @Pattern(regexp = "^\\S.*", message = "Customer email first character cannot be space")
    private String custEmail;
}