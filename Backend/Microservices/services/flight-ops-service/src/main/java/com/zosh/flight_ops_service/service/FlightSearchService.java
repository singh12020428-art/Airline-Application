package com.zosh.flight_ops_service.service;

import com.zosh.payload.request.FlightSearchRequest;
import com.zosh.payload.response.FlightInstanceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FlightSearchService {


    Page<FlightInstanceResponse> searchFlights(FlightSearchRequest request, Pageable pageable);

    com.zosh.event.FlightStatusUpdatedEvent getFlightStatus(String flightNumber, java.time.LocalDate date);

    java.util.List<com.zosh.event.FlightStatusUpdatedEvent> getAllFlightStatuses(java.time.LocalDate date);
}
