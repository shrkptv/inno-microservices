package dev.shrkptv.orderservice.services;

import dev.shrkptv.orderservice.client.UserServiceClient;
import dev.shrkptv.orderservice.database.entity.Item;
import dev.shrkptv.orderservice.database.entity.Order;
import dev.shrkptv.orderservice.database.entity.OrderItem;
import dev.shrkptv.orderservice.database.enums.OrderStatus;
import dev.shrkptv.orderservice.database.repository.ItemRepository;
import dev.shrkptv.orderservice.database.repository.OrderRepository;
import dev.shrkptv.orderservice.dto.OrderCreateDTO;
import dev.shrkptv.orderservice.dto.OrderItemCreateDTO;
import dev.shrkptv.orderservice.dto.OrderResponseDTO;
import dev.shrkptv.orderservice.dto.OrderUpdateDTO;
import dev.shrkptv.orderservice.dto.UserResponseDTO;
import dev.shrkptv.orderservice.exception.OrderNotFoundByIdException;
import dev.shrkptv.orderservice.mapper.OrderItemMapper;
import dev.shrkptv.orderservice.mapper.OrderMapper;
import dev.shrkptv.orderservice.services.impl.OrderServiceImpl;
import dev.shrkptv.orderservice.services.kafka.OrderProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private  UserServiceClient userServiceClient;
    @Mock
    private OrderProducer orderProducer;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("Create new order")
    void createOrder_success() {
        String testEmail = "test@gmail.com";
        Long itemId = 1L;

        Item mockItem = new Item();
        mockItem.setId(itemId);
        mockItem.setPrice(new BigDecimal("100.00"));
        mockItem.setName("Test Product");

        // Подготовка DTO
        OrderItemCreateDTO itemDto = new OrderItemCreateDTO();
        itemDto.setItemId(itemId);
        itemDto.setQuantity(2);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setItems(List.of(itemDto));

        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1L);
        userResponseDTO.setEmail(testEmail);

        Order order = new Order();
        order.setOrderItems(new ArrayList<>());

        OrderResponseDTO expectedResponse = new OrderResponseDTO();
        expectedResponse.setId(10L);
        expectedResponse.setStatus(OrderStatus.NEW);

        // MOCKING
        when(userServiceClient.getUserByEmail(testEmail)).thenReturn(userResponseDTO);
        when(orderMapper.toEntity(orderCreateDTO)).thenReturn(order);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(mockItem));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(any(Order.class))).thenReturn(expectedResponse);

        OrderResponseDTO result = orderService.createOrder(orderCreateDTO, testEmail);

        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(userResponseDTO, result.getUser());

        verify(userServiceClient).getUserByEmail(testEmail);
        verify(itemRepository).findById(itemId);
        verify(orderRepository).save(any(Order.class));
        verify(orderProducer).sendCreateOrderEvent(any(Order.class));
    }

    @Test
    @DisplayName("Throw exception when user was not found")
    void createOrder_throwsException_whenUserNotFound() {
        String testEmail = "test@gmail.com";
        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setUserEmail(testEmail);

        when(userServiceClient.getUserByEmail(testEmail)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> orderService.createOrder(orderCreateDTO,  testEmail));

        verify(userServiceClient).getUserByEmail("test@gmail.com");
    }

    @Test
    @DisplayName("Return order by id")
    void getOrderById_returnsOrderResponse() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);

        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1L);

        OrderResponseDTO orderResponseDTO = new OrderResponseDTO();
        orderResponseDTO.setId(1L);

        when(orderRepository.getOrderById(1L)).thenReturn(Optional.of(order));
        when(userServiceClient.getUserById(1L)).thenReturn(userResponseDTO);
        when(orderMapper.toDto(order)).thenReturn(orderResponseDTO);

        OrderResponseDTO result = orderService.getOrderById(1L);

        assertEquals(orderResponseDTO.getId(), result.getId());
        assertEquals(userResponseDTO, result.getUser());
        verify(orderRepository).getOrderById(1L);
        verify(userServiceClient).getUserById(1L);
        verify(orderMapper).toDto(order);
    }

    @Test
    @DisplayName("Throw exception when order was not found by id")
    void getOrderById_throwsOrderNotFoundException() {
        when(orderRepository.getOrderById(3L)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundByIdException.class, () -> orderService.getOrderById(3L));
        verify(orderRepository).getOrderById(3L);
    }

    @Test
    @DisplayName("Return list of orders by id list")
    void getOrderListById_returnsListForSpecificId() {
        Order order = new Order();
        order.setId(1L);

        when(orderRepository.findOrderByIdIn(List.of(1L))).thenReturn(List.of(order));
        when(orderMapper.toDto(order)).thenReturn(new OrderResponseDTO());

        assertEquals(1, orderService.getOrderListById(List.of(1L)).size());
        verify(orderRepository).findOrderByIdIn(List.of(1L));
        verify(orderMapper).toDto(order);
    }

    @Test
    @DisplayName("Return empty list when id list has no orders")
    void getOrderListById_returnsEmptyList(){
        when(orderRepository.findOrderByIdIn(List.of(1L, 2L))).thenReturn(List.of());
        assertTrue(orderService.getOrderListById(List.of(1L, 2L)).isEmpty());
        verify(orderRepository).findOrderByIdIn(List.of(1L, 2L));
    }

    @Test
    @DisplayName("Return list of orders by status")
    void getOrderListByStatus_returnsOrdersList(){
        Order order = new Order();
        order.setId(1L);

        when(orderRepository.findAllByOrderStatus(OrderStatus.NEW)).thenReturn(List.of(order));
        when(orderMapper.toDto(order)).thenReturn(new OrderResponseDTO());

        assertEquals(1, orderService.getOrderListByStatus(OrderStatus.NEW).size());
        verify(orderRepository).findAllByOrderStatus(OrderStatus.NEW);
        verify(orderMapper).toDto(order);
    }

    @Test
    @DisplayName("Return empty list when status has no orders")
    void getOrderListByStatus_returnsEmptyList() {
        when(orderRepository.findAllByOrderStatus(OrderStatus.NEW)).thenReturn(List.of());
        assertTrue(orderService.getOrderListByStatus(OrderStatus.NEW).isEmpty());
        verify(orderRepository).findAllByOrderStatus(OrderStatus.NEW);
    }

    @Test
    @DisplayName("Update existing order")
    void updateOrder_returnsUpdatedOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setOrderStatus(OrderStatus.NEW);

        OrderUpdateDTO orderUpdateDTO = new OrderUpdateDTO();
        orderUpdateDTO.setOrderStatus(OrderStatus.CANCELLED);

        UserResponseDTO userDTO = new UserResponseDTO();
        userDTO.setId(1L);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(userServiceClient.getUserById(1L)).thenReturn(userDTO);
        when(orderMapper.toDto(order)).thenReturn(new OrderResponseDTO());

        orderService.updateOrder(1L, orderUpdateDTO);
        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());

        verify(orderRepository).findById(order.getId());
        verify(orderRepository).save(order);
        verify(userServiceClient).getUserById(1L);
        verify(orderMapper).toDto(order);
    }

    @Test
    @DisplayName("Throw exception when updating order was not found")
    void updateOrder_throwsOrderNotFoundException()
    {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundByIdException.class, () -> orderService.updateOrder(1L, new OrderUpdateDTO()));
        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("Delete existing order")
    void deleteOrder_success() {
        when(orderRepository.existsById(2L)).thenReturn(true);
        doNothing().when(orderRepository).deleteById(2L);

        assertDoesNotThrow(() -> orderService.deleteOrder(2L));

        verify(orderRepository).existsById(2L);
        verify(orderRepository).deleteById(2L);
    }

    @Test
    @DisplayName("Throw exception when deleting order not found by id")
    void deleteOrder_throwsOrderNotFoundException(){
        when(orderRepository.existsById(2L)).thenReturn(false);
        assertThrows(OrderNotFoundByIdException.class, () -> orderService.deleteOrder(2L));
        verify(orderRepository).existsById(2L);
    }

    @Test
    @DisplayName("Return list of orders for specific user email")
    void getOrderListByEmail_success() {
        String email = "test@gmail.com";
        Long userId = 1L;

        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(userId);
        userResponseDTO.setEmail(email);

        Order order = new Order();
        order.setId(100L);
        order.setUserId(userId);

        OrderResponseDTO orderResponseDTO = new OrderResponseDTO();
        orderResponseDTO.setId(100L);

        when(userServiceClient.getUserByEmail(email)).thenReturn(userResponseDTO);
        when(orderRepository.findAllByUserId(userId)).thenReturn(List.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderResponseDTO);

        List<OrderResponseDTO> result = orderService.getOrderListByEmail(email);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userResponseDTO, result.get(0).getUser());

        verify(userServiceClient).getUserByEmail(email);
        verify(orderRepository).findAllByUserId(userId);
        verify(orderMapper).toDto(order);
    }
}