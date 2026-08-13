package com.zosh.flight_ops_service.service;

import com.zosh.payload.request.FlightScheduleRequest;
import com.zosh.payload.response.FlightScheduleResponse;

import java.util.List;

public interface FlightScheduleService {

    FlightScheduleResponse createFlightSchedule(Long userId,
                                                FlightScheduleRequest request) throws Exception;

    FlightScheduleResponse getFlightScheduleById(Long id) throws Exception;

    List<FlightScheduleResponse> getFlightScheduleByAirline(Long userId, String roles);

    FlightScheduleResponse updateFlightSchedule(Long id,
                                                FlightScheduleRequest flightScheduleRequest) throws Exception;

    void deleteFlightSchedule(Long id) throws Exception;
}
