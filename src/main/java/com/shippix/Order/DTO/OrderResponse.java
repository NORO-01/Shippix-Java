// src/main/java/com/shippix/Order/DTO/OrderResponse.java
package com.shippix.Order.DTO;

import com.shippix.Order.Model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private Long businessOwnerId;
    private String businessOwnerName;
    private String businessOwnerEmail;
    private String orderDescription;
    private String notesToDriver;
    private Double packageWeight;
    private Double packageValue;
    private Double deliveryDistance;
    private String deliveryAddress;
    private Double fromLatitude;
    private Double fromLongitude;
    private Double toLatitude;
    private Double toLongitude;
    private Order.Status status;

    // Assigned warehouse info
    private Long assignedWarehouseId;
    private String assignedWarehouseName;

    // Shipment info (if any)
    private Long shipmentId;
    private String shipmentType;
    private String shipmentStatus;

    private String custName;
    private String custPhoneNumber;
    private String custEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderResponse fromOrder(Order order) {
        return new OrderResponse(
                order.getOrderId(),
                order.getBusinessOwner().getId(),
                order.getBusinessOwner().getUsername(),
                order.getBusinessOwner().getEmail(),
                order.getOrderDescription(),
                order.getNotesToDriver(),
                order.getPackageWeight(),
                order.getPackageValue(),
                order.getDeliveryDistance(),
                order.getDeliveryAddress(),
                order.getFromLatitude(),
                order.getFromLongitude(),
                order.getToLatitude(),
                order.getToLongitude(),
                order.getStatus(),
                order.getAssignedWarehouse() != null ? order.getAssignedWarehouse().getWarehouseId() : null,
                order.getAssignedWarehouse() != null ? order.getAssignedWarehouse().getName() : null,
                order.getShipment() != null ? order.getShipment().getShipmentId() : null,
                order.getShipment() != null ? order.getShipment().getType().name() : null,
                order.getShipment() != null ? order.getShipment().getStatus().name() : null,
                order.getCustName(),
                order.getCustPhoneNumber(),
                order.getCustEmail(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
