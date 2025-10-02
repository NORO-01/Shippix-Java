package com.shippix.Payment.repo;
import com.shippix.Payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepo extends JpaRepository<Payment, Long>
{
    Optional<Payment> findByCode(String code);
    Optional<Payment> findByOrderReqId(Long orderReqId);
}

