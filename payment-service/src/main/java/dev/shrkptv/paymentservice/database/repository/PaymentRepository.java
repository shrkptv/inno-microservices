package dev.shrkptv.paymentservice.database.repository;

import dev.shrkptv.paymentservice.database.entity.Payment;
import dev.shrkptv.paymentservice.database.enums.PaymentStatus;
import dev.shrkptv.paymentservice.dto.TotalAmountDTO;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    List<Payment> findAllByOrderId(Long orderId);

    List<Payment> findAllByUserId(Long userId);

    List<Payment> findAllByStatusIn(List<PaymentStatus> statuses);

    @Aggregation(pipeline = {
            "{ $match: { timestamp: { $gte: ?0, $lte: ?1 }, status: 'SUCCESS' } }",
            "{ $group: { _id: null, totalAmount: { $sum: '$payment_amount' } } }"
    })
    TotalAmountDTO getTotalAmount(LocalDateTime startDate, LocalDateTime endDate);
}