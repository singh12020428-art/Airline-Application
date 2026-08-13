package com.zosh.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import com.zosh.payload.response.FlightMealResponse;
import com.zosh.payload.response.FlightCabinAncillaryResponse;

import java.util.List;

@FeignClient(name = "ancillary-service")
public interface AncillaryClient {

    @PostMapping("/api/flight-cabin-ancillaries/price/total")
    Double calculateAncillariesPrice(
            @RequestBody List<Long> flightCabinAncillaryIds);

    @PostMapping("/api/flight-meals/price/total")
     Double calculateMealPrice(
            @RequestBody List<Long> requests
    );

    @GetMapping("/api/flight-meals")
    List<FlightMealResponse> getMealsByIds(@RequestParam("ids") List<Long> ids);

    @GetMapping("/api/flight-cabin-ancillaries/{id}")
    FlightCabinAncillaryResponse getAncillaryById(@PathVariable("id") Long id);
}
