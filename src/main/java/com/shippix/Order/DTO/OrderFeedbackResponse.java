package com.shippix.Order.DTO;

import java.time.LocalDateTime;

public record OrderFeedbackResponse(
        Long orderId,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
    public static OrderFeedbackResponse fromEntity(com.shippix.Order.Model.OrderFeedback feedback) {
        return new OrderFeedbackResponse(
                feedback.getOrder().getOrderId(),
                feedback.getRating(),
                feedback.getComment(),
                feedback.getCreatedAt()
        );
    }
}
