package com.shippix.Order.DTO;

import jakarta.validation.constraints.*;

public record OrderFeedbackRequest(
        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        Integer rating,

        @Size(max = 1000, message = "Comment must be less than 1000 characters")
        String comment
) {}
