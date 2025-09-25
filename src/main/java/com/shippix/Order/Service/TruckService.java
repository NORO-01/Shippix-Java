package com.shippix.Order.Service;

import com.shippix.Order.Model.Truck;
import com.shippix.Order.Model.Warehouse;
import com.shippix.Order.Repo.TruckRepo;
import com.shippix.Order.Repo.WarehouseRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TruckService {

    private final TruckRepo truckRepo;
    private final WarehouseRepo warehouseRepo;



    public Truck createTruck(Truck truck) {
        truck.setStatus(Truck.Status.AVAILABLE);
        return truckRepo.save(truck);
    }

//    public Truck createTruck(Long warehouseId, Truck truck) {
//        Warehouse warehouse = warehouseRepo.findById(warehouseId)
//                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));
//
//        truck.setWarehouse(warehouse);
//        truck.setStatus(Truck.Status.AVAILABLE);
//        return truckRepo.save(truck);
//    }

    public void updateStatus(Truck truck, Truck.Status status) {
        truck.setStatus(status);
        truckRepo.save(truck);
    }

    public List<Truck> getAvailableTrucks() {
        return truckRepo.findByStatus(Truck.Status.AVAILABLE);
    }

    public Truck getTruck(Long id) {
        return truckRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Truck not found"));
    }

    public List<Truck> getAllTrucks() {
        return truckRepo.findAll();
    }

    public List<Truck> getTrucksByWarehouse(Long warehouseId) {
        return truckRepo.findByWarehouse_WarehouseId(warehouseId);
    }
}

