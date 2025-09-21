package com.shippix.Order.Service;

import com.shippix.Order.DTO.ShipmentResponse;
import com.shippix.Order.Model.Order;
import com.shippix.Order.Model.Shipment;
import com.shippix.Order.Model.Truck;
import com.shippix.Order.Model.Warehouse;
import com.shippix.Order.Repo.OrderRepo;
import com.shippix.Order.Repo.ShipmentRepo;
import com.shippix.Order.Repo.TruckRepo;
import com.shippix.Order.Repo.WarehouseRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepo shipmentRepo;
    private final OrderRepo orderRepo;
    private final TruckRepo truckRepo;
    private final WarehouseRepo warehouseRepo;

    /**
     * Create a shipment (either PICKUP or DELIVERY).
     */
    @Transactional
    public ShipmentResponse createShipment(Long warehouseId, Long truckId, List<Long> orderIds, Shipment.Type type) {
        Warehouse warehouse = warehouseRepo.findById(warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));

        Truck truck = truckRepo.findById(truckId)
                .orElseThrow(() -> new EntityNotFoundException("Truck not found"));

        if (truck.getStatus() != Truck.Status.AVAILABLE) {
            throw new IllegalStateException("Truck not available for shipment");
        }

        List<Order> orders = orderRepo.findAllById(orderIds);

        if (orders.isEmpty()) {
            throw new IllegalArgumentException("No valid orders provided");
        }

        // Create shipment
        Shipment shipment = new Shipment();
        shipment.setWarehouse(warehouse);
        shipment.setTruck(truck);
        shipment.setOrders(orders);
        shipment.setType(type);
        shipment.setStatus(Shipment.Status.PLANNED);

        Shipment saved = shipmentRepo.save(shipment);

        // Update truck status
        truck.setStatus(Truck.Status.IN_TRANSIT);
        truckRepo.save(truck);

        // Update orders status based on shipment type
        if (type == Shipment.Type.PICKUP) {
            orders.forEach(o -> o.setStatus(Order.Status.IN_PROGRESS));
        } else if (type == Shipment.Type.DELIVERY) {
            orders.forEach(o -> o.setStatus(Order.Status.OUT_FOR_DELIVERY));
        }
        orderRepo.saveAll(orders);

        return ShipmentResponse.fromShipment(saved);
    }

    /**
     * Start a shipment (truck leaves).
     */
    @Transactional
    public ShipmentResponse startShipment(Long shipmentId) {
        Shipment shipment = shipmentRepo.findById(shipmentId)
                .orElseThrow(() -> new EntityNotFoundException("Shipment not found"));

        if (shipment.getStatus() != Shipment.Status.PLANNED) {
            throw new IllegalStateException("Shipment not in PLANNED state");
        }

        shipment.setStatus(Shipment.Status.IN_PROGRESS);
        shipment.setStartedAt(LocalDateTime.now());

        return ShipmentResponse.fromShipment(shipmentRepo.save(shipment));
    }

    /**
     * Complete a shipment (truck returns).
     */
    @Transactional
    public ShipmentResponse completeShipment(Long shipmentId) {
        Shipment shipment = shipmentRepo.findById(shipmentId)
                .orElseThrow(() -> new EntityNotFoundException("Shipment not found"));

        if (shipment.getStatus() != Shipment.Status.IN_PROGRESS) {
            throw new IllegalStateException("Shipment not in progress");
        }

        // Orders update depending on shipment type
        if (shipment.getType() == Shipment.Type.PICKUP) {
            shipment.getOrders().forEach(o -> {
                o.setStatus(Order.Status.IN_WAREHOUSE);
                o.setAssignedWarehouse(shipment.getWarehouse());
            });
        } else if (shipment.getType() == Shipment.Type.DELIVERY) {
            shipment.getOrders().forEach(o -> o.setStatus(Order.Status.DELIVERED));
        }
        orderRepo.saveAll(shipment.getOrders());

        // Truck back to available
        Truck truck = shipment.getTruck();
        truck.setStatus(Truck.Status.AVAILABLE);
        truckRepo.save(truck);

        // Finalize shipment
        shipment.setStatus(Shipment.Status.COMPLETED);
        shipment.setCompletedAt(LocalDateTime.now());
        return ShipmentResponse.fromShipment(shipmentRepo.save(shipment));
    }

    /**
     * Cancel a shipment (rollback).
     */
    @Transactional
    public ShipmentResponse cancelShipment(Long shipmentId) {
        Shipment shipment = shipmentRepo.findById(shipmentId)
                .orElseThrow(() -> new EntityNotFoundException("Shipment not found"));

        if (shipment.getStatus() == Shipment.Status.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed shipment");
        }

        shipment.setStatus(Shipment.Status.CANCELED);

        // Reset truck to available if it was locked
        Truck truck = shipment.getTruck();
        if (truck.getStatus() == Truck.Status.IN_TRANSIT) {
            truck.setStatus(Truck.Status.AVAILABLE);
            truckRepo.save(truck);
        }

        // Rollback orders depending on shipment type
        if (shipment.getType() == Shipment.Type.PICKUP) {
            shipment.getOrders().forEach(o -> o.setStatus(Order.Status.IN_PROGRESS));
        } else if (shipment.getType() == Shipment.Type.DELIVERY) {
            shipment.getOrders().forEach(o -> o.setStatus(Order.Status.IN_WAREHOUSE));
        }
        orderRepo.saveAll(shipment.getOrders());

        return ShipmentResponse.fromShipment(shipmentRepo.save(shipment));
    }
}


