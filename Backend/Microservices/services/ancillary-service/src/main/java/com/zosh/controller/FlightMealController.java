package com.zosh.controller;

import com.zosh.payload.request.FlightMealRequest;
import com.zosh.payload.response.FlightMealResponse;
import com.zosh.services.FlightMealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/flight-meals")
public class FlightMealController {

    private final FlightMealService flightMealService;

    @PostMapping
    public ResponseEntity<FlightMealResponse> createFlightMeal(
            @Valid @RequestBody FlightMealRequest flightMealRequest
    ) throws Exception {
        FlightMealResponse response = flightMealService.createFlightMeal(flightMealRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/price/total")
    public ResponseEntity<Double> calculateMealPrice(
            @RequestBody List<Long> requests
    ) {
        double responses = flightMealService.calculateMealPrice(requests);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightMealResponse> getFlightMealById(
            @PathVariable Long id
    ) throws Exception {
        return ResponseEntity.ok(flightMealService.getFlightMealById(id));
    }

    @GetMapping("/flight/{flightId}")
    public ResponseEntity<List<FlightMealResponse>> getMealsByFlightId(
            @PathVariable Long flightId
    ) {
        return ResponseEntity.ok(flightMealService.getByFlightId(flightId));
    }

    @GetMapping
    public ResponseEntity<List<FlightMealResponse>> getMealsByIds(
            @RequestParam List<Long> ids
    ) {
        return ResponseEntity.ok(flightMealService.getAllByIds(ids));
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<FlightMealResponse> updateFlightMealAvailability(
            @PathVariable Long id,
            @RequestParam Boolean available
    ) throws Exception {
        return ResponseEntity.ok(flightMealService.updateFlightMealAvailability(id, available));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlightMealResponse> updateFlightMeal(
            @PathVariable Long id,
            @Valid @RequestBody FlightMealRequest flightMealRequest
    ) throws Exception {
        return ResponseEntity.ok(flightMealService.updateFlightMeal(id, flightMealRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlightMeal(
            @PathVariable Long id
    ) throws Exception {
        flightMealService.deleteFlightMeal(id);
        return ResponseEntity.noContent().build();
    }
}
