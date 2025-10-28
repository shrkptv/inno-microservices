package dev.shrkptv.apigateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service", url = "${auth-client.url}")
public interface AuthFeignClient {

    @PostMapping("/validate")
    ResponseEntity<Boolean> validateToken(@RequestParam String token);
}
