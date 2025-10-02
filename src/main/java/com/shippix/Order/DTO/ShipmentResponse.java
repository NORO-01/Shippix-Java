package com.shippix.Order.DTO;

import com.shippix.Order.Model.Order;
import com.shippix.Order.Model.Shipment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentResponse {
    private Long shipmentId;
    private Long truckId;
    private Long warehouseId;
    private List<Long> orderIds;
    private String type;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public static ShipmentResponse fromShipment(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getShipmentId(),
                shipment.getTruck().getTruckId(),
                shipment.getWarehouse().getWarehouseId(),
                shipment.getOrders().stream().map(Order::getOrderId).toList(),
                shipment.getType().name(),
                shipment.getStatus().name(),
                shipment.getCreatedAt(),
                shipment.getStartedAt(),
                shipment.getCompletedAt()
        );
    }
}
