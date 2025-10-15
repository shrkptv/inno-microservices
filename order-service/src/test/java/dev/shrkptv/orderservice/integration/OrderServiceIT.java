package dev.shrkptv.orderservice.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import dev.shrkptv.orderservice.database.entity.Order;
import dev.shrkptv.orderservice.database.entity.OrderStatus;
import dev.shrkptv.orderservice.database.repository.OrderRepository;
import dev.shrkptv.orderservice.dto.OrderCreateDTO;
import dev.shrkptv.orderservice.dto.OrderResponseDTO;
import dev.shrkptv.orderservice.dto.OrderUpdateDTO;
import dev.shrkptv.orderservice.services.OrderService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.get;

import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
public class OrderServiceIT {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new  PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("order_service")
            .withUsername("test")
            .withPassword("test");

    static WireMockServer wireMockServer = new WireMockServer(8081);

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeAll
    static void startWireMock(){
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock(){
        wireMockServer.stop();
    }

    @BeforeEach
    void setupMocks() {

        configureFor("localhost", wireMockServer.port());

        stubFor(get(urlPathEqualTo("/api/users/email"))
                .withQueryParam("email", equalTo("test@gmail.com"))
                .willReturn(okJson("""
                    {
                        "id": 1,
                        "email": "test@gmail.com"
                    }
                """)));

        stubFor(get(urlPathEqualTo("/api/users/email"))
                .withQueryParam("email", equalTo("testfail@gmail.com"))
                .willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

        stubFor(get(urlMatching("/api/users/1"))
                .willReturn(okJson("""
                    {
                        "id": 1,
                        "email": "test@gmail.com"
                    }
                """)));
    }

    @AfterEach
    void cleanup() {
        wireMockServer.resetAll();
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("Create new order")
    void createOrder(){
        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setUserEmail("test@gmail.com");
        orderCreateDTO.setItems(List.of());

        OrderResponseDTO orderResponseDTO = orderService.createOrder(orderCreateDTO);

        assertEquals(1, orderResponseDTO.getUser().getId());
        assertEquals(1, orderRepository.findAll().size());

        verify(getRequestedFor(urlPathEqualTo("/api/users/email"))
                .withQueryParam("email", equalTo("test@gmail.com")));
    }

    @Test
    @DisplayName("Throw exception when user not found by email")
    void createOrder_userNotFound(){
        OrderCreateDTO orderCreateDTO= new OrderCreateDTO();
        orderCreateDTO.setUserEmail("testfail@gmail.com");

        assertThrows(RuntimeException.class, () -> orderService.createOrder(orderCreateDTO));

        verify(getRequestedFor(urlPathEqualTo("/api/users/email"))
                .withQueryParam("email", equalTo("testfail@gmail.com")));
    }

    @Test
    @DisplayName("Update order status")
    void getOrderById(){
        Order order = new Order();
        order.setUserId(1L);
        order.setOrderStatus(OrderStatus.NEW);
        order.setCreationDate(LocalDate.now());
        orderRepository.save(order);

        OrderUpdateDTO orderUpdateDTO = new OrderUpdateDTO();
        orderUpdateDTO.setOrderStatus(OrderStatus.DELIVERED);

        OrderResponseDTO orderResponseDTO = orderService.updateOrder(order.getId(), orderUpdateDTO);

        assertEquals(OrderStatus.DELIVERED, orderResponseDTO.getStatus());
        assertEquals(OrderStatus.DELIVERED, orderRepository.getOrderById(order.getId()).get().getOrderStatus());
    }

    @Test
    @DisplayName("Delete order by id")
    void deleteOrder(){
        Order order = new Order();
        order.setOrderStatus(OrderStatus.NEW);
        order.setCreationDate(LocalDate.now());
        order.setUserId(1L);
        orderRepository.save(order);

        orderService.deleteOrder(order.getId());
        assertFalse(orderRepository.getOrderById(order.getId()).isPresent());
    }


}
