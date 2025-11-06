package dev.shrkptv.authservice.client;

import dev.shrkptv.authservice.dto.UserCreateRequestDTO;
import dev.shrkptv.authservice.dto.UserResponseDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserFeignClient {

    @PostMapping
    ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateRequestDTO userCreateDTO);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteUser(@PathVariable Long id);
}
