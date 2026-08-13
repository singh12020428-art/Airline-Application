package com.zosh.flight_ops_service.repository;

import com.zosh.flight_ops_service.model.FlightSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlightScheduleRepository extends JpaRepository<FlightSchedule, Long> {
    List<FlightSchedule> findByFlightAirlineId(Long airlineId);
}
