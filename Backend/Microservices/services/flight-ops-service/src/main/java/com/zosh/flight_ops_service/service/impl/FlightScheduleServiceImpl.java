package com.zosh.flight_ops_service.service.impl;

import com.zosh.enums.FlightStatus;
import com.zosh.flight_ops_service.client.AirlineClient;
import com.zosh.flight_ops_service.client.LocationClient;
import com.zosh.flight_ops_service.mapper.FlightScheduleMapper;
import com.zosh.flight_ops_service.model.Flight;
import com.zosh.flight_ops_service.model.FlightSchedule;
import com.zosh.payload.request.FlightScheduleRequest;
import com.zosh.payload.request.FlightInstanceRequest;
import com.zosh.payload.response.AirlineResponse;
import com.zosh.payload.response.AirportResponse;
import com.zosh.payload.response.FlightScheduleResponse;
import com.zosh.flight_ops_service.repository.FlightRepository;
import com.zosh.flight_ops_service.repository.FlightScheduleRepository;
import com.zosh.flight_ops_service.service.FlightInstanceService;
import com.zosh.flight_ops_service.service.FlightScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightScheduleServiceImpl implements FlightScheduleService {

    private final FlightScheduleRepository flightScheduleRepository;
    private final FlightRepository flightRepository;
    private final FlightInstanceService flightInstanceService;
    private final LocationClient locationClient;
    private final AirlineClient airlineClient;

    @Override
    public FlightScheduleResponse createFlightSchedule(Long userId,
                                                       FlightScheduleRequest request) throws Exception {
        AirlineResponse airlineResponse = airlineClient.getAirlineByOwner(userId);
        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new Exception("Flight not found with given id"));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new Exception("End date cannot be before start date");
        }

        FlightSchedule flightSchedule = FlightScheduleMapper.toEntity(request, flight);
        FlightSchedule savedSchedule = flightScheduleRepository.save(flightSchedule);

        // Generate flight instances for each operating day between start and end date
        List<DayOfWeek> operatingDays = savedSchedule.getOperatingDays();
        LocalDate startDate = savedSchedule.getStartDate();
        LocalDate endDate = savedSchedule.getEndDate();

        FlightInstanceRequest flightInstanceRequest = FlightInstanceRequest.builder()
                .scheduleId(savedSchedule.getId())
                .flightId(flight.getId())
                .arrivalAirportId(savedSchedule.getArrivalAirportId())
                .departureAirportId(savedSchedule.getDepartureAirportId())
                .totalSeats(90)
                .status(FlightStatus.SCHEDULED)
                .build();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (operatingDays.contains(date.getDayOfWeek())) {

                flightInstanceRequest.setDepartureDateTime(LocalDateTime.of(date, savedSchedule.getDepartureTime()));
                flightInstanceRequest.setArrivalDateTime(LocalDateTime.of(date, savedSchedule.getArrivalTime()));
                flightInstanceService.createFlightInstance(userId, flightInstanceRequest);
            }
        }

        return convertToFlightScheduleResponse(savedSchedule);
    }

    @Override
    public FlightScheduleResponse getFlightScheduleById(Long id) throws Exception {
        FlightSchedule flightSchedule = flightScheduleRepository.findById(id)
                .orElseThrow(() -> new Exception("Flight schedule not found with id " + id));
        return convertToFlightScheduleResponse(flightSchedule);
    }

    @Override
    public List<FlightScheduleResponse> getFlightScheduleByAirline(Long userId, String roles) {
        List<FlightSchedule> schedules;
        if (roles != null && roles.contains("ROLE_SYSTEM_ADMIN")) {
            schedules = flightScheduleRepository.findAll();
        } else {
            AirlineResponse airlineResponse = airlineClient.getAirlineByOwner(userId);
            schedules = flightScheduleRepository.findByFlightAirlineId(airlineResponse.getId());
        }
        return schedules.stream()
                .map(this::convertToFlightScheduleResponse)
                .toList();
    }

    @Override
    public FlightScheduleResponse updateFlightSchedule(Long id, FlightScheduleRequest request) throws Exception {
        FlightSchedule flightSchedule = flightScheduleRepository.findById(id)
                .orElseThrow(() -> new Exception("Flight schedule not found with id " + id));

        FlightScheduleMapper.updateEntity(request, flightSchedule);
        FlightSchedule updatedSchedule = flightScheduleRepository.save(flightSchedule);
        return convertToFlightScheduleResponse(updatedSchedule);
    }

    @Override
    public void deleteFlightSchedule(Long id) throws Exception {
        FlightSchedule flightSchedule = flightScheduleRepository.findById(id)
                .orElseThrow(() -> new Exception("Flight schedule not found with id " + id));
        flightScheduleRepository.delete(flightSchedule);
    }

    private FlightScheduleResponse convertToFlightScheduleResponse(FlightSchedule flightSchedule) {
        AirlineResponse airline = null;
        if (flightSchedule.getFlight() != null && flightSchedule.getFlight().getAirlineId() != null) {
            try {
                airline = airlineClient.getAirlineById(flightSchedule.getFlight().getAirlineId());
            } catch (Exception e) {
                airline = new AirlineResponse();
                airline.setId(flightSchedule.getFlight().getAirlineId());
                airline.setName("Unknown Airline");
            }
        }

        AirportResponse departureAirport = null;
        try {
            departureAirport = locationClient.getAirportById(flightSchedule.getDepartureAirportId());
        } catch (Exception e) {
            departureAirport = new AirportResponse();
            departureAirport.setId(flightSchedule.getDepartureAirportId());
            departureAirport.setIataCode("UNK");
            com.zosh.payload.response.CityResponse city = new com.zosh.payload.response.CityResponse();
            city.setName("Unknown City");
            departureAirport.setCity(city);
        }

        AirportResponse arrivalAirport = null;
        try {
            arrivalAirport = locationClient.getAirportById(flightSchedule.getArrivalAirportId());
        } catch (Exception e) {
            arrivalAirport = new AirportResponse();
            arrivalAirport.setId(flightSchedule.getArrivalAirportId());
            arrivalAirport.setIataCode("UNK");
            com.zosh.payload.response.CityResponse city = new com.zosh.payload.response.CityResponse();
            city.setName("Unknown City");
            arrivalAirport.setCity(city);
        }

        return FlightScheduleMapper.toResponse(flightSchedule, airline, arrivalAirport, departureAirport);
    }
}
