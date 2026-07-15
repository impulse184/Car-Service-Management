package com.carservice.operations.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@FeignClient(name = "user-profile-service")
public interface UserProfileClient {

    @GetMapping("/users/userprofile/{id}")
    Map<String, Object> getProfileById(@PathVariable("id") Long id);
}
