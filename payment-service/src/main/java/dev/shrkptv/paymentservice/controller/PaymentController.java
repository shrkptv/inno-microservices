package dev.shrkptv.paymentservice.controller;

import dev.shrkptv.paymentservice.database.enums.PaymentStatus;
import dev.shrkptv.paymentservice.dto.PaymentCreateDTO;
import dev.shrkptv.paymentservice.dto.PaymentResponseDTO;
import dev.shrkptv.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentCreateDTO paymentCreateDTO) {
        PaymentResponseDTO paymentResponseDTO = paymentService.createPayment(paymentCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentResponseDTO);
    }

    @GetMapping("/order/{orderId}")
    ResponseEntity<List<PaymentResponseDTO>> getPaymentsByOrderId(@PathVariable Long orderId) {
        List<PaymentResponseDTO> paymentResponseDTOList = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(paymentResponseDTOList);
    }

    @GetMapping("/user/{userId}")
    ResponseEntity<List<PaymentResponseDTO>> getPaymentsByUserId(@PathVariable Long userId) {
        List<PaymentResponseDTO> paymentResponseDTOList = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(paymentResponseDTOList);
    }

    @GetMapping("/statuses")
    ResponseEntity<List<PaymentResponseDTO>> getPaymentByStatuses(@RequestParam List<PaymentStatus> statuses) {
        List<PaymentResponseDTO> paymentResponseDTOList = paymentService.getPaymentsByStatuses(statuses);
        return ResponseEntity.ok(paymentResponseDTOList);
    }

    @GetMapping("/total")
    ResponseEntity<BigDecimal> getTotalAmountForPeriod(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        BigDecimal total = paymentService.getTotalAmountForPeriod(startDate, endDate);
        return ResponseEntity.ok(total);
    }
}
