package dev.shrkptv.paymentservice.database.entity;

import dev.shrkptv.paymentservice.database.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "payments")
public class Payment {
    @Id
    private String id;

    @Field("order_id")
    private Long orderId;
    @Field("user_id")
    private Long userId;
    private PaymentStatus status;
    private LocalDateTime timestamp;

    @Field(name = "payment_amount", targetType = FieldType.DECIMAL128)
    private BigDecimal paymentAmount;
}
