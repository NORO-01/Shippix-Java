package com.shippix.Order.Repo;

import com.shippix.Order.Model.OrderRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRequestRepo extends JpaRepository<OrderRequest, Long> {
    List<OrderRequest> findByBusinessOwnerId(Long businessOwnerId);
}