package com.shippix.Analytics.Service;

import com.shippix.Analytics.DTO.DashboardStatsDTO;
import com.shippix.Order.Model.Order;
import com.shippix.Order.Repo.OrderRepo;
import com.shippix.Order.Model.OrderFeedback;
import com.shippix.Order.Repo.OrderFeedbackRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService
{
    private final OrderRepo orderRepo;
    private final OrderFeedbackRepo feedbackRepo;

    public DashboardStatsDTO getDashboardStats()
    {
        List<Order> allOrders = orderRepo.findAll();

        int totalCompleted = (int) allOrders.stream()
                .filter(o -> o.getStatus() == Order.Status.DELIVERED)
                .count();

        int pendingPickup = (int) allOrders.stream()
                .filter(o -> o.getStatus() == Order.Status.IN_PROGRESS)
                .count();

        int completedToday = (int) allOrders.stream()
                .filter(o -> o.getStatus() == Order.Status.DELIVERED
                        && o.getUpdatedAt().toLocalDate().equals(LocalDate.now()))
                .count();

        int active = (int) allOrders.stream()
                .filter(o -> o.getStatus() == Order.Status.OUT_FOR_DELIVERY)
                .count();

        double deliverySuccessRate = allOrders.isEmpty() ? 0.0 :
                (double) totalCompleted / allOrders.size() * 100;

        double avgDeliveryTime = allOrders.stream()
                .filter(o -> o.getStatus() == Order.Status.DELIVERED)
                .mapToDouble(o ->
                        o.getUpdatedAt().toLocalDate().toEpochDay() - o.getCreatedAt().toLocalDate().toEpochDay()
                ).average().orElse(0.0);


        double customerSatisfaction = feedbackRepo.findAll().stream()
                .filter(f -> f.getRating() != null)
                .mapToInt(OrderFeedback::getRating)
                .average()
                .orElse(0.0);

        return new DashboardStatsDTO(totalCompleted, pendingPickup, completedToday, active,
                deliverySuccessRate, avgDeliveryTime, customerSatisfaction);
    }
}
