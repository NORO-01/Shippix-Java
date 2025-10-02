package com.shippix.Order.Repo;

import com.shippix.Order.Model.OrderFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderFeedbackRepo extends JpaRepository<OrderFeedback, Long> {
    boolean existsByOrder_OrderId(Long orderId);
    OrderFeedback findByOrder_OrderId(Long orderId);
    List<OrderFeedback> findByOrder_BusinessOwner_Id(Long businessOwnerId);
}
