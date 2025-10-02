package com.shippix.Analytics.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDTO
{
    private int totalCompletedOrders;
    private int pendingPickup;
    private int completedToday;
    private int activeOrders;

    private double deliverySuccessRate; //0.0 - 100.0
    private double averageDeliveryTime; //in days
    private double customerSatisfaction; //0.0 - 5.0
}