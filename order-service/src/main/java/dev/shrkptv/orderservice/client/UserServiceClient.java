package dev.shrkptv.orderservice.client;

import dev.shrkptv.orderservice.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserServiceClient {

    @GetMapping("/email")
    UserResponseDTO getUserByEmail(@RequestParam String email);

    @GetMapping("/{id}")
    UserResponseDTO getUserById(@PathVariable("id") Long id);
}
