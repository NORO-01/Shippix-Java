package com.shippix;

import com.shippix.Order.Service.RoutingService;
import com.shippix.Order.Service.RoutingService.TruckRoute;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RoutingTestRunner implements CommandLineRunner {

    private final RoutingService routingService;

    @Override
    public void run(String... args) {
        Long warehouseId = 1L;  
        int maxOrdersPerTruck = 20;

        System.out.println("=== Running RoutingService Test ===");

        try {
            List<TruckRoute> routes = routingService.routeWarehouseOrders(warehouseId, maxOrdersPerTruck);

            for (int i = 0; i < routes.size(); i++) {
                TruckRoute tr = routes.get(i);
                System.out.println("Truck #" + (i + 1));
                System.out.println("Truck ID: " + tr.getTruck().getTruckId());
                System.out.println("Truck Status: " + tr.getTruck().getStatus());
                System.out.println("Warehouse ID: " + tr.getTruck().getWarehouse().getWarehouseId());
                System.out.println("Truck Capacity: " + tr.getTruck().getCapacityWeight());
                System.out.println("Direction: " + tr.getDirection());
                System.out.println("Distance bucket: " + tr.getDistanceBucket() + " km");
                System.out.println("Orders count: " + tr.getOrders().size());
                tr.getOrders().forEach(o -> 
                        System.out.println("   OrderID: " + o.getOrderId() + " | Addr: " + o.getDeliveryAddress() + 
                                           " | Weight: " + o.getPackageWeight() + " | Value: " + o.getPackageValue() +
                                           " | Dist: " + o.getDeliveryDistance() + " km"
                        ));
                System.out.println("---------------------------------------------------");
            }

        } catch (Exception e) {
            System.err.println("Error running routing test: " + e.getMessage());
        }

        System.out.println("=== Test Finished ===");
    }
}
