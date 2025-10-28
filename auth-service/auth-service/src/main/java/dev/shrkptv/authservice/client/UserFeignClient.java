package dev.shrkptv.authservice.client;

import dev.shrkptv.authservice.dto.RegisterResponseDTO;
import dev.shrkptv.authservice.dto.UserCreateRequestDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserFeignClient {

    @PostMapping
    ResponseEntity<RegisterResponseDTO> createUser(@Valid @RequestBody UserCreateRequestDTO userCreateDTO);
}
