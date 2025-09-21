package com.shippix.Order.Service;

import com.shippix.Order.DTO.OrderCreateRequest;
import com.shippix.Order.DTO.OrderResponse;
import com.shippix.Order.Model.Order;
import com.shippix.Order.Model.OrderRequest;
import com.shippix.Order.Repo.OrderRepo;
import com.shippix.Order.Repo.OrderRequestRepo;
import com.shippix.Order.Util.Haversine;
import com.shippix.User_Management.Model.BusinessOwner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderRequestService {

    private final OrderRequestRepo orderRequestRepo;
    private final OrderRepo orderRepo;


    public OrderRequest submitRequest(BusinessOwner businessOwner, OrderCreateRequest dto) {
        OrderRequest req = new OrderRequest();
        req.setBusinessOwner(businessOwner);
        req.setOrderDescription(dto.getOrderDescription());
        req.setNotesToDriver(dto.getNotesToDriver());
        req.setPackageWeight(dto.getPackageWeight());
        req.setPackageValue(dto.getPackageValue());
        req.setFromLatitude(dto.getFromLatitude());
        req.setFromLongitude(dto.getFromLongitude());
        req.setToLatitude(dto.getToLatitude());
        req.setToLongitude(dto.getToLongitude());
        req.setCustName(dto.getCustName());
        req.setCustPhoneNumber(dto.getCustPhoneNumber());
        req.setCustEmail(dto.getCustEmail());
        req.setDecision(OrderRequest.Status.PENDING);

        double distanceKm = Haversine.distanceInKm(
                dto.getFromLatitude(),
                dto.getFromLongitude(),
                dto.getToLatitude(),
                dto.getToLongitude()
        );
        req.setDeliveryDistance(distanceKm);
        return orderRequestRepo.save(req);
    }

    @Transactional
    public OrderResponse approveRequest(Long reqId) {
        OrderRequest req = orderRequestRepo.findById(reqId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (req.getDecision() != OrderRequest.Status.PENDING) {
            throw new RuntimeException("Request already processed");
        }

        req.setDecision(OrderRequest.Status.APPROVED);
        req.setReviewedAt(LocalDateTime.now());
        orderRequestRepo.save(req);

        // Convert into real Order
        Order order = new Order();
        order.setBusinessOwner(req.getBusinessOwner());
        order.setOrderDescription(req.getOrderDescription());
        order.setNotesToDriver(req.getNotesToDriver());
        order.setPackageWeight(req.getPackageWeight());
        order.setPackageValue(req.getPackageValue());
        order.setDeliveryDistance(req.getDeliveryDistance());
        order.setFromLatitude(req.getFromLatitude());
        order.setFromLongitude(req.getFromLongitude());
        order.setToLatitude(req.getToLatitude());
        order.setToLongitude(req.getToLongitude());
        order.setCustName(req.getCustName());
        order.setCustPhoneNumber(req.getCustPhoneNumber());
        order.setCustEmail(req.getCustEmail());
        order.setStatus(Order.Status.ACCEPTED);

        Order savedOrder = orderRepo.save(order);
        return OrderResponse.fromOrder(savedOrder);
    }

//    Reject request (remains an OrderRequest, never becomes an Order)
    @Transactional
    public OrderRequest rejectRequest(Long reqId) {
        OrderRequest req = orderRequestRepo.findById(reqId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (req.getDecision() != OrderRequest.Status.PENDING) {
            throw new RuntimeException("Request already processed");
        }

        req.setDecision(OrderRequest.Status.REJECTED);
        req.setReviewedAt(LocalDateTime.now());

        return orderRequestRepo.save(req);
    }

//    List all requests (raw requests, not orders)
    public List<OrderRequest> getAllRequests() {
        return orderRequestRepo.findAll();
    }

//    List requests by Business Owner
    public List<OrderRequest> listByBusinessOwner(Long boId) {
        return orderRequestRepo.findByBusinessOwnerId(boId);
    }

//    Get specific request by ID and Business Owner (for security)
    public OrderRequest getRequestByIdAndBusinessOwner(Long reqId, Long businessOwnerId) {
        return orderRequestRepo.findById(reqId)
                .filter(req -> req.getBusinessOwner().getId().equals(businessOwnerId))
                .orElseThrow(() -> new RuntimeException("Request not found or access denied"));
    }

//    Get specific request by ID (for admin)
    public OrderRequest getRequestById(Long reqId) {
        return orderRequestRepo.findById(reqId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
    }
}
