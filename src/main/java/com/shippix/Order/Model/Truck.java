package com.shippix.Order.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "trucks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Truck {

    public enum Status {
        AVAILABLE,
        IN_TRANSIT,
        MAINTENANCE,
        UNAVAILABLE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "truck_id")
    private Long truckId;

    @Column(name = "license_plate", unique = true, nullable = false)
    private String licensePlate;

    @Column(name = "model")
    private String model;

    @Column(name = "capacity_weight")
    private Double capacityWeight;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String currentLocation;

    @Column(name = "assigned_route_id")
    private Long assignedRouteId;

    private Boolean isDeleted = false;
    private LocalDateTime deletedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // RELATIONS

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    @JsonIgnore
    private Warehouse warehouse;

//    @OneToMany(mappedBy = "assignedTruck")
//    private List<Order> orders;
}
