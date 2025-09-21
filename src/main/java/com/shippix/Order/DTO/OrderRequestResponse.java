package com.shippix.Order.DTO;

import com.shippix.Order.Model.OrderRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestResponse {
    private Long reqId;
    private Long businessOwnerId;
    private String businessOwnerName;
    private String businessOwnerEmail;
    private String orderDescription;
    private String notesToDriver;
    private Double packageWeight;
    private Double packageValue;
    private Double deliveryDistance;
    private Double fromLatitude;
    private Double fromLongitude;
    private Double toLatitude;
    private Double toLongitude;
    private String custName;
    private String custPhoneNumber;
    private String custEmail;
    private OrderRequest.Status decision;
    private LocalDateTime reviewedAt;

    public static OrderRequestResponse fromOrderRequest(OrderRequest orderRequest) {
        return new OrderRequestResponse(
                orderRequest.getReqId(),
                orderRequest.getBusinessOwner().getId(),
                orderRequest.getBusinessOwner().getUsername(),
                orderRequest.getBusinessOwner().getEmail(),
                orderRequest.getOrderDescription(),
                orderRequest.getNotesToDriver(),
                orderRequest.getPackageWeight(),
                orderRequest.getPackageValue(),
                orderRequest.getDeliveryDistance(),
                orderRequest.getFromLatitude(),
                orderRequest.getFromLongitude(),
                orderRequest.getToLatitude(),
                orderRequest.getToLongitude(),
                orderRequest.getCustName(),
                orderRequest.getCustPhoneNumber(),
                orderRequest.getCustEmail(),
                orderRequest.getDecision(),
                orderRequest.getReviewedAt()
        );
    }
}
