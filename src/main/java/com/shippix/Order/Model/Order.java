package com.shippix.Order.Model;

import com.shippix.User_Management.Model.BusinessOwner;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    public enum Status {
//        PENDING,
        ACCEPTED,
//        REJECTED,
        IN_PROGRESS,
        PICKED_UP,
        IN_WAREHOUSE,
        OUT_FOR_DELIVERY,
        DELIVERED,
        CANCELED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "business_owner_id", nullable = false)
    private BusinessOwner businessOwner;

    @Column(name = "order_description")
    private String orderDescription;

    @Column(name = "notes_to_driver")
    private String notesToDriver;

    @Column(name = "package_weight")
    private Double packageWeight;

    @Column(name = "package_value")
    private Double packageValue;

    @Column(name = "delivery_distance")
    private Double deliveryDistance;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    // Coordinates
    private Double fromLatitude;
    private Double fromLongitude;
    private Double toLatitude;
    private Double toLongitude;

    @Enumerated(EnumType.STRING)
    private Status status;

    // Assigned warehouse (optional until first assignment)
    @ManyToOne
    @JoinColumn(name = "assigned_warehouse_id")
    private Warehouse assignedWarehouse;

    // Orders can be part of ONE active shipment (pickup or delivery)
    @ManyToOne
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;


//    @ManyToOne
//    @JoinColumn(name = "assigned_warehouse_id")
//    private Warehouse assignedWarehouse;
//
//    @ManyToOne
//    @JoinColumn(name = "assigned_truck_id")
//    private Truck assignedTruck;

    private Boolean isDeleted = false;
    private LocalDateTime deletedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String custName;
    private String custPhoneNumber;
    private String custEmail;
}
