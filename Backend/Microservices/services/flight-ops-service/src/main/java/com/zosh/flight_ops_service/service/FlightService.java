package com.zosh.flight_ops_service.service;

import com.zosh.enums.FlightStatus;
import com.zosh.payload.request.FlightRequest;
import com.zosh.payload.response.FlightResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FlightService {

    FlightResponse createFlight(Long userId, FlightRequest flightRequest) throws Exception;

    Page<FlightResponse> getFlightsByAirline(Long userId, String roles,
                                             Long departureAirportId,
                                             Long arrivalAirportId,
                                             Pageable pageable);

    FlightResponse getFlightById(Long id) throws Exception;

    FlightResponse updateFlight(Long id, FlightRequest flightRequest, String roles) throws Exception;

    FlightResponse changeStatus(Long id, FlightStatus status, String roles) throws Exception;

    void deleteFlight(Long userId, Long id, String roles) throws Exception;
}
