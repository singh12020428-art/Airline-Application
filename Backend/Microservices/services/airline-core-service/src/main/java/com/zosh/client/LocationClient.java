package com.zosh.client;

import com.zosh.payload.response.CityResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "location-service")
public interface LocationClient {
    @GetMapping("/api/cities/{id}")
    CityResponse getCityById(@PathVariable Long id);
}
