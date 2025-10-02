package com.shippix.Payment.controller;

import com.shippix.Order.Model.OrderRequest;
import com.shippix.Order.Repo.OrderRequestRepo;
import com.shippix.Payment.dto.PriceResponse;
import com.shippix.Payment.service.PricingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")
public class PricingController
{
    private final PricingService pricingService;
    private final OrderRequestRepo orderRequestRepo;

    public PricingController(PricingService pricingService, OrderRequestRepo orderRequestRepo)
    {
        this.pricingService = pricingService;
        this.orderRequestRepo = orderRequestRepo;
    }

    @GetMapping("/{reqId}")
    public ResponseEntity<PriceResponse> calculatePrice(@PathVariable Long reqId)
    {
        //get order req by req id
        OrderRequest orderRequest = orderRequestRepo.findById(reqId)
                .orElseThrow(() -> new RuntimeException("OrderRequest not found"));

        //calc price
        double price = pricingService.calculatePrice(orderRequest);

        //update calculated price fe order req
        orderRequest.setCalculatedPrice(price);
        orderRequestRepo.save(orderRequest);

        return ResponseEntity.ok(new PriceResponse(reqId, price, orderRequest.getCurrency()));
    }
}

