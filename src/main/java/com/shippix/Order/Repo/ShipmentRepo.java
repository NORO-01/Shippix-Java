package com.shippix.Order.Repo;

import com.shippix.Order.Model.Shipment;
import com.shippix.Order.Model.Warehouse;
import com.shippix.Order.Model.Truck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentRepo extends JpaRepository<Shipment, Long> {
    List<Shipment> findByWarehouse(Warehouse warehouse);
    List<Shipment> findByTruck(Truck truck);
    List<Shipment> findByStatus(Shipment.Status status);
    List<Shipment> findByType(Shipment.Type type);
    List<Shipment> findByWarehouseAndStatus(Warehouse warehouse, Shipment.Status status);
}

