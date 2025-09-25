package com.shippix.Order.Repo;

import com.shippix.Order.Model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {
    List<Order> findByBusinessOwnerId(Long businessOwnerId);
    List<Order> findByStatus(Order.Status status);
    List<Order> findByIsDeletedFalse();
    List<Order> findByAssignedWarehouse_WarehouseId(Long warehouseId);
    List<Order> findByAssignedWarehouse_WarehouseIdAndStatus(Long warehouseId, Order.Status status);
    @Query("SELECT o FROM Order o WHERE o.assignedWarehouse.warehouseId = :warehouseId AND o.status = 'IN_WAREHOUSE' ORDER BY o.createdAt DESC")
    List<Order> findInWarehouseOrdersByWarehouseSorted(@Param("warehouseId") Long warehouseId);

}