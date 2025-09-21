package com.shippix.Order.Repo;

import com.shippix.Order.Model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {
    List<Order> findByBusinessOwnerId(Long businessOwnerId);
    List<Order> findByStatus(Order.Status status);
    List<Order> findByIsDeletedFalse();
}