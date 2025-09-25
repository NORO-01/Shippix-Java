package com.shippix;

import com.shippix.Order.Model.Order;
import com.shippix.Order.Repo.OrderRepo;
import com.shippix.Order.Model.Warehouse;
import com.shippix.Order.Repo.WarehouseRepo;
import com.shippix.User_Management.Model.BusinessOwner;
import com.shippix.User_Management.Model.BusinessType;
import com.shippix.User_Management.Model.Users;

import io.jsonwebtoken.lang.Arrays;

import com.shippix.Order.Model.Truck;
import com.shippix.Order.Repo.TruckRepo;
import com.shippix.User_Management.Repo.UserRepo;
import com.shippix.User_Management.Model.Users;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final WarehouseRepo warehouseRepo;
    private final TruckRepo truckRepo;
    private final OrderRepo orderRepo;
    private final UserRepo userRepository;


    @Override
    public void run(String... args) {

    BusinessOwner owner = new BusinessOwner();
    owner.setUsername("owner1");
    owner.setPassword("{noop}owner123");
    owner.setEmail("owner1@example.com");
    owner.setRole(Users.Role.ROLE_BUSINESS_OWNER);
    owner.setBusinessName("Tech Store");
    owner.setBusinessType(BusinessType.ELECTRONICS_STORE);
    owner.setLatitude(30.0444); // Cairo example
    owner.setLongitude(31.2357); 
    userRepository.save(owner);
    System.out.println("Business owner created: " + owner.getUsername() + " / owner123");

        // Insert Warehouse
        Warehouse warehouse = new Warehouse();
        warehouse.setCapacityWeight(10000.0);
        warehouse.setCurrentOccupancyWeight(2500.0);
        warehouse.setLatitude(30.0444);
        warehouse.setLongitude(31.2357);
        warehouse.setAddress("Cairo, Nasr City");
        warehouse.setName("Cairo Central Warehouse");
        warehouse.setStatus(Warehouse.Status.ACTIVE);
        warehouseRepo.save(warehouse);

        // Insert Trucks
        Warehouse w = warehouseRepo.findById(1L).orElseThrow();
        List<Truck> trucks = List.of(
            new Truck(null, "CAI-1234", "Isuzu NQR", 300.0, Truck.Status.AVAILABLE, "Cairo Ring Road", null, false, null, null, null, w),
            new Truck(null, "CAI-5678", "Toyota Dyna", 250.0, Truck.Status.IN_TRANSIT, "Cairo Downtown", null, false, null, null, null, w),
            new Truck(null, "ALX-1122", "Mercedes Atego", 300.0, Truck.Status.AVAILABLE, "Alexandria Corniche", null, false, null, null, null, w),
            new Truck(null, "ALX-3344", "Mitsubishi Fuso", 280.0, Truck.Status.MAINTENANCE, "Alexandria Port", null, false, null, null, null, w),
            new Truck(null, "GIZ-5566", "Isuzu Elf", 220.0, Truck.Status.AVAILABLE, "Giza Haram Street", null, false, null, null, null, w)
        );

        truckRepo.saveAll(trucks);

        List<Order> orders = new ArrayList<>();

        for (int i = 1; i <= 30; i++) {
            Order order = new Order();
            order.setBusinessOwner(owner);
            order.setOrderDescription("Order " + i + " - Electronics/Clothes");
            order.setNotesToDriver("Note for order " + i);
            order.setPackageWeight(1.0 + (i * 0.5));
            if(i % 5 == 0) order.setPackageWeight(50.0 + (i * 1.5)); // heavier packages for some orders
            order.setPackageValue(100.0 + (i * 20));
            order.setDeliveryDistance(5.0 + (i * 0.7));
            order.setDeliveryAddress("Address " + i + ", City " + i );
            order.setFromLatitude(30.0 + (i * 0.01));
            order.setFromLongitude(31.0 + (i * 0.01));
            order.setToLatitude(30.05 + (i * 0.01));
            order.setToLongitude(31.05 + (i * 0.01));
            if(i < 23) order.setStatus(Order.Status.IN_WAREHOUSE);
            else order.setStatus(Order.Status.OUT_FOR_DELIVERY);
            order.setAssignedWarehouse(w);
            order.setCreatedAt(LocalDateTime.now().minusDays(i));
            order.setCustName("Customer " + i);
            order.setCustEmail("cust" + i + "@gmail.com");
            order.setCustPhoneNumber("01000000" + String.format("%02d", i));
            orders.add(order);
        }
        orderRepo.saveAll(orders);


        System.out.println("✅ Seeding done: warehouse, trucks, and 30 orders inserted.");
    }
}
