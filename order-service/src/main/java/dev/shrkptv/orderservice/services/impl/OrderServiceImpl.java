package dev.shrkptv.orderservice.services.impl;

import dev.shrkptv.orderservice.client.UserServiceClient;
import dev.shrkptv.orderservice.database.entity.Order;
import dev.shrkptv.orderservice.database.entity.OrderItem;
import dev.shrkptv.orderservice.database.enums.OrderStatus;
import dev.shrkptv.orderservice.database.repository.OrderRepository;
import dev.shrkptv.orderservice.dto.OrderCreateDTO;
import dev.shrkptv.orderservice.dto.OrderResponseDTO;
import dev.shrkptv.orderservice.dto.OrderUpdateDTO;
import dev.shrkptv.orderservice.dto.UserResponseDTO;
import dev.shrkptv.orderservice.exception.OrderNotFoundByIdException;
import dev.shrkptv.orderservice.exception.UserNotFoundByEmailException;
import dev.shrkptv.orderservice.mapper.OrderItemMapper;
import dev.shrkptv.orderservice.mapper.OrderMapper;
import dev.shrkptv.orderservice.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserServiceClient userServiceClient;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderCreateDTO orderCreateDTO) {
        UserResponseDTO userResponseDTO = userServiceClient.getUserByEmail(orderCreateDTO.getUserEmail());

        if (userResponseDTO == null || userResponseDTO.getId() == null)
            throw new UserNotFoundByEmailException(orderCreateDTO.getUserEmail());

        Order order = orderMapper.toEntity(orderCreateDTO);
        order.setUserId(userResponseDTO.getId());
        order.setOrderStatus(OrderStatus.NEW);
        order.setCreationDate(LocalDate.now());

        List<OrderItem> orderItems = orderItemMapper.toEntityList(orderCreateDTO.getItems());
        orderItems.forEach(item -> item.setOrder(order));
        order.setOrderItems(orderItems);

        OrderResponseDTO orderResponseDTO = orderMapper.toDto(orderRepository.save(order));
        orderResponseDTO.setUser(userResponseDTO);

        return orderResponseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.getOrderById(id).orElseThrow(() -> new OrderNotFoundByIdException(id));
        UserResponseDTO userResponseDTO = userServiceClient.getUserById(order.getUserId());
        OrderResponseDTO orderResponseDTO = orderMapper.toDto(order);
        orderResponseDTO.setUser(userResponseDTO);
        return orderResponseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrderListById(List<Long> idList) {
        return orderRepository.findOrderByIdIn(idList)
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrderListByStatus(OrderStatus status) {
        return orderRepository.findAllByOrderStatus(status)
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrder(Long id, OrderUpdateDTO orderUpdateDTO) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundByIdException(id));
        order.setOrderStatus(orderUpdateDTO.getOrderStatus());
        orderRepository.save(order);

        UserResponseDTO user = userServiceClient.getUserById(order.getUserId());
        OrderResponseDTO response = orderMapper.toDto(order);
        response.setUser(user);
        return response;
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundByIdException(id);
        }

        orderRepository.deleteById(id);
    }
}
