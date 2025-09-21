package com.shippix.Order.Repo;

import com.shippix.Order.Model.Truck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TruckRepo extends JpaRepository<Truck, Long> {
    List<Truck> findByWarehouse_WarehouseId(Long warehouseId);
    List<Truck> findByStatus(Truck.Status status);
    List<Truck> findByIsDeletedFalse();
    List<Truck> findByWarehouse_WarehouseIdAndStatus(Long warehouseId, Truck.Status status);
}