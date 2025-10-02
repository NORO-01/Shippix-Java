package com.shippix.Analytics.Controller;

import com.shippix.Analytics.DTO.DashboardStatsDTO;
import com.shippix.Analytics.Service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController
{
    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(analyticsService.getDashboardStats());
    }
}