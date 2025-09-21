package com.shippix.Order.Controller;

import com.shippix.Order.DTO.ShipmentResponse;
import com.shippix.Order.Model.Shipment;
import com.shippix.Order.Service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(
            @RequestParam Long warehouseId,
            @RequestParam Long truckId,
            @RequestBody List<Long> orderIds,
            @RequestParam Shipment.Type type) {
        
        ShipmentResponse shipment = shipmentService.createShipment(warehouseId, truckId, orderIds, type);
        return ResponseEntity.status(HttpStatus.CREATED).body(shipment);
    }

    @PostMapping("/{shipmentId}/start")
    public ResponseEntity<ShipmentResponse> startShipment(@PathVariable Long shipmentId) {
        ShipmentResponse shipment = shipmentService.startShipment(shipmentId);
        return ResponseEntity.ok(shipment);
    }

    @PostMapping("/{shipmentId}/complete")
    public ResponseEntity<ShipmentResponse> completeShipment(@PathVariable Long shipmentId) {
        ShipmentResponse shipment = shipmentService.completeShipment(shipmentId);
        return ResponseEntity.ok(shipment);
    }

    @PostMapping("/{shipmentId}/cancel")
    public ResponseEntity<ShipmentResponse> cancelShipment(@PathVariable Long shipmentId) {
        ShipmentResponse shipment = shipmentService.cancelShipment(shipmentId);
        return ResponseEntity.ok(shipment);
    }

    @GetMapping
    public ResponseEntity<List<ShipmentResponse>> getAllShipments() {
        // Note: This would need a method in ShipmentService to get all shipments
        // For now, returning empty list - you can implement this in the service if needed
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{shipmentId}")
    public ResponseEntity<ShipmentResponse> getShipmentById(@PathVariable Long shipmentId) {
        // Note: This would need a method in ShipmentService to get shipment by ID
        // For now, returning null - you can implement this in the service if needed
        return ResponseEntity.ok(null);
    }
}
