package com.zosh.controller;

import com.zosh.payload.request.FlightInstanceCabinRequest;
import com.zosh.payload.response.FlightInstanceCabinResponse;
import com.zosh.service.FlightInstanceCabinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/flight-instance-cabins")
public class FlightInstanceCabinController {

    private final FlightInstanceCabinService flightInstanceCabinService;

    @PostMapping
    public ResponseEntity<FlightInstanceCabinResponse> createFlightInstanceCabin(
            @Valid @RequestBody FlightInstanceCabinRequest request) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flightInstanceCabinService.createFlightInstanceCabin(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightInstanceCabinResponse> getFlightInstanceCabinById(@PathVariable Long id) {
        return ResponseEntity.ok(flightInstanceCabinService.getFlightInstanceCabinById(id));
    }

    @GetMapping("/flight-instance/{flightInstanceId}")
    public ResponseEntity<Page<FlightInstanceCabinResponse>> getByFlightInstanceId(
            @PathVariable Long flightInstanceId, Pageable pageable) {
        return ResponseEntity.ok(flightInstanceCabinService.getByFlightInstanceId(flightInstanceId, pageable));
    }

    @GetMapping("/flight-instance/{flightInstanceId}/cabin-class/{cabinClassId}")
    public ResponseEntity<FlightInstanceCabinResponse> getByFlightInstanceIdAndCabinClassId(
            @PathVariable Long flightInstanceId, @PathVariable Long cabinClassId) {
        return ResponseEntity.ok(
                flightInstanceCabinService.getByFlightInstanceIdAndCabinClassId(flightInstanceId, cabinClassId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlightInstanceCabinResponse> updateFlightInstanceCabin(
            @PathVariable Long id, @RequestBody FlightInstanceCabinRequest request) {
        return ResponseEntity.ok(flightInstanceCabinService.updateFlightInstanceCabin(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlightInstanceCabin(@PathVariable Long id) {
        flightInstanceCabinService.deleteFlightInstanceCabin(id);
        return ResponseEntity.noContent().build();
    }
}
