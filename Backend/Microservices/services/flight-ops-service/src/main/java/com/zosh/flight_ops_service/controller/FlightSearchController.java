package com.zosh.flight_ops_service.controller;

import com.zosh.enums.CabinClassType;
import com.zosh.payload.request.FlightSearchRequest;
import com.zosh.payload.response.FlightInstanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.zosh.flight_ops_service.service.FlightSearchService;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/flights")
public class FlightSearchController {

    private final FlightSearchService flightSearchService;

    @GetMapping("/search")
    public ResponseEntity<Page<FlightInstanceResponse>> searchFlights(
            @RequestParam Long departureAirportId,
            @RequestParam Long arrivalAirportId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
            @RequestParam Integer passengers,
            @RequestParam CabinClassType cabinClass,
            @RequestParam(required = false) List<Long> airlines,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String departureTimeRange,
            @RequestParam(required = false) String arrivalTimeRange,
            @RequestParam(required = false) Integer maxDuration,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) String sortBy,
            Pageable pageable
    ) {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .departureAirportId(departureAirportId)
                .arrivalAirportId(arrivalAirportId)
                .departureDate(departureDate)
                .passengers(passengers)
                .cabinClass(cabinClass)
                .airlines(airlines)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .departureTimeRange(departureTimeRange)
                .arrivalTimeRange(arrivalTimeRange)
                .maxDuration(maxDuration)
                .sortOrder(sortOrder)
                .sortBy(sortBy)
                .build();

        return ResponseEntity.ok(
                flightSearchService.searchFlights(request, pageable)
        );
    }
    @GetMapping("/status")
    public ResponseEntity<com.zosh.event.FlightStatusUpdatedEvent> getFlightStatus(
            @RequestParam String flightNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(flightSearchService.getFlightStatus(flightNumber, date));
    }

    @GetMapping("/status/all")
    public ResponseEntity<List<com.zosh.event.FlightStatusUpdatedEvent>> getAllFlightStatuses(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(flightSearchService.getAllFlightStatuses(date));
    }
}
