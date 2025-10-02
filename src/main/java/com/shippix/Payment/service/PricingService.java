package com.shippix.Payment.service;

import com.shippix.Order.Model.OrderRequest;
import com.shippix.Order.Util.Haversine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PricingService
{
    @Value("${pricing.basePrice}")
    private double basePrice;

    @Value("${pricing.perKmRate}")
    private double perKmRate;

    @Value("${pricing.perKgRate}")
    private double perKgRate;

    @Value("${pricing.insuranceRate}")
    private double insuranceRate;

    public double calculatePrice(OrderRequest orderRequest)
    {
        double price = basePrice
                + (orderRequest.getDeliveryDistance() * perKmRate)
                + (orderRequest.getPackageWeight() * perKgRate)
                + (orderRequest.getPackageValue() * insuranceRate);

        return Math.round(price * 100.0) / 100.0;
    }
}