package com.shippix.Order.Service;

import com.shippix.Order.DTO.OrderFeedbackRequest;
import com.shippix.Order.DTO.OrderFeedbackResponse;
import com.shippix.Order.Model.Order;
import com.shippix.Order.Model.OrderFeedback;
import com.shippix.Order.Repo.OrderFeedbackRepo;
import com.shippix.Order.Repo.OrderRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderFeedbackService {

    private final OrderRepo orderRepo;
    private final OrderFeedbackRepo feedbackRepo;

    //  Add feedback to a completed or canceled order
    public OrderFeedbackResponse addFeedback(Long orderId, OrderFeedbackRequest dto) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!(order.getStatus() == Order.Status.DELIVERED || order.getStatus() == Order.Status.CANCELED)) {
            throw new IllegalStateException("Feedback can only be added to completed or canceled orders");
        }

        if (feedbackRepo.existsByOrder_OrderId(orderId)) {
            throw new IllegalStateException("Feedback already exists for this order");
        }

        OrderFeedback feedback = new OrderFeedback();
        feedback.setOrder(order);
        feedback.setRating(dto.rating());
        feedback.setComment(dto.comment());

        return OrderFeedbackResponse.fromEntity(feedbackRepo.save(feedback));
    }

    // Get feedback for an order
    public OrderFeedbackResponse getFeedback(Long orderId) {
        OrderFeedback feedback = feedbackRepo.findByOrder_OrderId(orderId);
        if (feedback == null) {
            throw new EntityNotFoundException("Feedback not found for this order");
        }
        return OrderFeedbackResponse.fromEntity(feedback);
    }

    public List<OrderFeedbackResponse> getFeedbacksForBusinessOwner(Long boId) {
        return feedbackRepo.findByOrder_BusinessOwner_Id(boId).stream()
                .map(OrderFeedbackResponse::fromEntity)
                .toList();
    }

    public OrderFeedbackResponse submitFeedback(Long orderId, Long businessOwnerId, OrderFeedbackRequest request) {
        Order order = orderRepo.findById(orderId)
                .filter(o -> o.getBusinessOwner().getId().equals(businessOwnerId))
                .orElseThrow(() -> new EntityNotFoundException("Order not found or access denied"));

        if (!(order.getStatus() == Order.Status.DELIVERED || order.getStatus() == Order.Status.CANCELED)) {
            throw new IllegalStateException("Feedback can only be added to completed or canceled orders");
        }

        if (feedbackRepo.existsByOrder_OrderId(orderId)) {
            throw new IllegalStateException("Feedback already exists for this order");
        }

        OrderFeedback feedback = new OrderFeedback();
        feedback.setOrder(order);
        feedback.setRating(request.rating());
        feedback.setComment(request.comment());

        return OrderFeedbackResponse.fromEntity(feedbackRepo.save(feedback));
    }

    public OrderFeedbackResponse getFeedbackByOrderIdAndBusinessOwner(Long orderId, Long businessOwnerId) {
        Order order = orderRepo.findById(orderId)
                .filter(o -> o.getBusinessOwner().getId().equals(businessOwnerId))
                .orElseThrow(() -> new EntityNotFoundException("Order not found or access denied"));

        OrderFeedback feedback = feedbackRepo.findByOrder_OrderId(orderId);
        if (feedback == null) {
            throw new EntityNotFoundException("Feedback not found for this order");
        }
        return OrderFeedbackResponse.fromEntity(feedback);
    }

}
