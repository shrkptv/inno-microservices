package dev.shrkptv.paymentservice.database.repository;

import dev.shrkptv.paymentservice.database.entity.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends MongoRepository<Payment,String> {

    List<Payment> findAllByOrderId(Long orderId);

    List<Payment> findAllByUserId(Long userId);

    List<Payment> findAllByStatusIn(List<String> status);

    @Query("{'timestamp': {'$gte': ?0, '$lte' : ?1}}")
    BigDecimal getTotalAmount(LocalDateTime start, LocalDateTime end);
}
