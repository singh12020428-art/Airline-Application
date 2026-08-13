package com.zosh.flight_ops_service.service;

import com.zosh.payload.request.FlightInstanceRequest;
import com.zosh.payload.response.FlightInstanceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface FlightInstanceService {

    FlightInstanceResponse createFlightInstance(Long userId,
                                                FlightInstanceRequest request) throws Exception;

    FlightInstanceResponse getFlightInstanceById(Long id) throws Exception;

    Page<FlightInstanceResponse> getByAirlineId(Long userId, String roles,
                                                Long departureAirportId,
                                                Long arrivalAirportId,
                                                Long flightId,
                                                LocalDate onDate,
                                                Pageable pageable);

    FlightInstanceResponse updateFlightInstance(Long id,
                                                FlightInstanceRequest request) throws Exception;

    void deleteFlightInstance(Long id) throws Exception;
}
