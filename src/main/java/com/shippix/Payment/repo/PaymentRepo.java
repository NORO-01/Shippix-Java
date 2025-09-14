package com.shippix.Payment.repo;
import com.shippix.Payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepo extends JpaRepository<Payment, String> {
    Optional<Payment> findByOrderId(String orderId);  //34an mawdo3 el idempotency
}
