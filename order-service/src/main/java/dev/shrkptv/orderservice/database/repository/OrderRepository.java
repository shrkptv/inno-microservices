package dev.shrkptv.orderservice.database.repository;

import dev.shrkptv.orderservice.database.entity.Order;
import dev.shrkptv.orderservice.database.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> getOrderById(Long id);

    List<Order> findOrderByIdIn(Collection<Long> idList);

    List<Order> findAllByOrderStatus(OrderStatus status);
}
