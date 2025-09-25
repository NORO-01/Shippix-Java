package com.shippix.Order.Controller;

import com.shippix.Order.Model.Truck;
import com.shippix.Order.Service.TruckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/trucks")
@RequiredArgsConstructor
public class TruckController {

    private final TruckService truckService;

    @PostMapping
    public ResponseEntity<Truck> createTruck(@RequestBody Truck truck) {
        Truck createdTruck = truckService.createTruck(truck);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTruck);
    }

    @GetMapping
    public ResponseEntity<List<Truck>> getAllTrucks() {
        List<Truck> trucks = truckService.getAllTrucks();
        return ResponseEntity.ok(trucks);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Truck>> getAvailableTrucks() {
        List<Truck> trucks = truckService.getAvailableTrucks();
        return ResponseEntity.ok(trucks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Truck> getTruckById(@PathVariable Long id) {
        Truck truck = truckService.getTruck(id);
        return ResponseEntity.ok(truck);
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<Truck>> getTrucksByWarehouse(@PathVariable Long warehouseId) {
        List<Truck> trucks = truckService.getTrucksByWarehouse(warehouseId);
        return ResponseEntity.ok(trucks);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Truck> updateTruckStatus(
            @PathVariable Long id,
            @RequestParam Truck.Status status) {
        Truck truck = truckService.getTruck(id);
        truckService.updateStatus(truck, status);
        return ResponseEntity.ok(truck);
    }
}
