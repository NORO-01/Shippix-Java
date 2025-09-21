package com.shippix.Order.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "warehouses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {

    public enum Status {
        ACTIVE,
        INACTIVE,
        FULL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_id")
    private Long warehouseId;

    private String name;
    private String address;
    private Double latitude;
    private Double longitude;

    @Column(name = "capacity_weight")
    private Double capacityWeight;

    @Column(name = "current_occupancy_weight")
    private Double currentOccupancyWeight;

    @Enumerated(EnumType.STRING)
    private Status status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // RELATIONS

    @OneToMany(mappedBy = "warehouse")
    private List<Truck> trucks;

    @OneToMany(mappedBy = "assignedWarehouse")
    private List<Order> orders;
}
