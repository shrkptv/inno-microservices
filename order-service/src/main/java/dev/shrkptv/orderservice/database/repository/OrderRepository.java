package dev.shrkptv.orderservice.database.repository;

import dev.shrkptv.orderservice.database.entity.Order;
import dev.shrkptv.orderservice.database.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findOrderByIdIn(Collection<Long> idList);

    List<Order> findOrdersByOrderStatusIn(Collection<OrderStatus> statusList);
}
