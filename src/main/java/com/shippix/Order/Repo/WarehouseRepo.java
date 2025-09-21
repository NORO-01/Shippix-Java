package com.shippix.Order.Repo;

import com.shippix.Order.Model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseRepo extends JpaRepository<Warehouse, Long> {
    List<Warehouse> findByStatus(Warehouse.Status status);
}