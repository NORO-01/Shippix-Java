// src/main/java/com/shippix/Order/Service/WarehouseOpsService.java
package com.shippix.Order.Service;

import com.shippix.Order.DTO.OrderResponse;
import com.shippix.Order.Model.Order;
import com.shippix.Order.Model.Warehouse;
import com.shippix.Order.Repo.OrderRepo;
import com.shippix.Order.Repo.WarehouseRepo;
import com.shippix.Order.Util.Haversine;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepo warehouseRepo;
    private final OrderRepo orderRepo;

    // Create a new warehouse
    public Warehouse createWarehouse(Warehouse warehouse) {
        warehouse.setCurrentOccupancyWeight(0.0);
        warehouse.setStatus(Warehouse.Status.ACTIVE);
        return warehouseRepo.save(warehouse);
    }

    // Get warehouse by id
    public Warehouse getWarehouse(Long id) {
        return warehouseRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));
    }

    // Get all warehouses
    public List<Warehouse> getAllWarehouses() {
        return warehouseRepo.findAll();
    }

    // Update warehouse (e.g., change name, address, capacity)
    public Warehouse updateWarehouse(Long id, Warehouse updates) {
        Warehouse existing = getWarehouse(id);
        existing.setName(updates.getName());
        existing.setAddress(updates.getAddress());
        existing.setLatitude(updates.getLatitude());
        existing.setLongitude(updates.getLongitude());
        existing.setCapacityWeight(updates.getCapacityWeight());
        return warehouseRepo.save(existing);
    }

    // Mark warehouse inactive
    public void deactivateWarehouse(Long id) {
        Warehouse warehouse = getWarehouse(id);
        warehouse.setStatus(Warehouse.Status.INACTIVE);
        warehouseRepo.save(warehouse);
    }

    public Warehouse assignWarehouse(Order order) {
        List<Warehouse> candidates = warehouseRepo.findByStatus(Warehouse.Status.ACTIVE);

        Warehouse best = null;
        double bestDistance = Double.MAX_VALUE;

        for (Warehouse wh : candidates) {
            double projectedWeight = wh.getCurrentOccupancyWeight() + order.getPackageWeight();
            if (projectedWeight > wh.getCapacityWeight()) continue;

            double dist = Haversine.distanceInKm(order.getFromLatitude(), order.getFromLongitude(),
                    wh.getLatitude(), wh.getLongitude())
                    + Haversine.distanceInKm(wh.getLatitude(), wh.getLongitude(),
                    order.getToLatitude(), order.getToLongitude());

            if (dist < bestDistance) {
                best = wh;
                bestDistance = dist;
            }
        }

        if (best == null) throw new IllegalStateException("No suitable warehouse");

        // update warehouse load
        best.setCurrentOccupancyWeight(best.getCurrentOccupancyWeight() + order.getPackageWeight());
        if (best.getCurrentOccupancyWeight() >= best.getCapacityWeight()*.95) {
            best.setStatus(Warehouse.Status.FULL);
        }
        warehouseRepo.save(best);

        // assign order
        order.setAssignedWarehouse(best);
        order.setStatus(Order.Status.IN_PROGRESS);
        orderRepo.save(order);

        return best;
    }

    public void updateOccupancy(Warehouse warehouse, double weightDelta) {
        warehouse.setCurrentOccupancyWeight(
                warehouse.getCurrentOccupancyWeight() + weightDelta
        );

        if (warehouse.getCurrentOccupancyWeight() >= warehouse.getCapacityWeight() * 0.95) {
            warehouse.setStatus(Warehouse.Status.FULL);
        } else {
            warehouse.setStatus(Warehouse.Status.ACTIVE);
        }

        warehouseRepo.save(warehouse);
    }
}
