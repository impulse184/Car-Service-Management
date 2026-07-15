package com.carservice.operations.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "car-details-validation-service")
public interface CarValidationClient {

    // send the carNumber to validation
    @GetMapping("/carvalidation/validate")
    boolean isCarNumberValid(@RequestParam("carNumber") String carNumber);
}
