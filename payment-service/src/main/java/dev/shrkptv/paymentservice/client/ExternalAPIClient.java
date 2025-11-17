package dev.shrkptv.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "external-api", url = "${external-api.url}")
public interface ExternalAPIClient {

    @GetMapping
    String generateRandomNumber();
}
