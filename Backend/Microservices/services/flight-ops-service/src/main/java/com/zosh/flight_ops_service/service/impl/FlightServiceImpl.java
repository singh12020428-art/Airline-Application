package com.zosh.flight_ops_service.service.impl;

import com.zosh.enums.FlightStatus;
import com.zosh.flight_ops_service.client.AirlineClient;
import com.zosh.flight_ops_service.client.LocationClient;
import com.zosh.flight_ops_service.mapper.FlightMapper;
import com.zosh.flight_ops_service.model.Flight;
import com.zosh.payload.request.FlightRequest;
import com.zosh.payload.response.AircraftResponse;
import com.zosh.payload.response.AirlineResponse;
import com.zosh.payload.response.AirportResponse;
import com.zosh.payload.response.FlightResponse;
import com.zosh.flight_ops_service.repository.FlightRepository;
import com.zosh.flight_ops_service.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final AirlineClient airlineClient;
    private final LocationClient locationClient;


    @Override
    public FlightResponse createFlight(Long userId, FlightRequest flightRequest) throws Exception {

        AirlineResponse airlineResponse = airlineClient.getAirlineByOwner(userId);
        if (flightRepository.existsByFlightNumber(flightRequest.getFlightNumber())) {
            throw new Exception("Flight with number already exists");
        }
        Flight flight = FlightMapper.toEntity(flightRequest);
        flight.setAirlineId(airlineResponse.getId());
        Flight saved = flightRepository.save(flight);
        return convertToFlightResponse(saved);
    }

    @Override
    public Page<FlightResponse> getFlightsByAirline(Long userId, String roles,
                                                    Long departureAirportId,
                                                    Long arrivalAirportId,
                                                    Pageable pageable) {
        if (roles != null && roles.contains("ROLE_SYSTEM_ADMIN")) {
            return flightRepository.findAllFlights(departureAirportId, arrivalAirportId, pageable)
                    .map(this::convertToFlightResponse);
        }

        AirlineResponse airlineResponse = airlineClient.getAirlineByOwner(userId);

        return flightRepository.findByAirlineId(
                airlineResponse.getId(), departureAirportId, arrivalAirportId, pageable
        ).map(this::convertToFlightResponse);
    }

    @Override
    public FlightResponse getFlightById(Long id) throws Exception {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new Exception("Flight not found with id " + id));
        return convertToFlightResponse(flight);
    }

    @Override
    public FlightResponse updateFlight(Long id, FlightRequest flightRequest, String roles) throws Exception {
        Flight existing = flightRepository.findById(id)
                .orElseThrow(() -> new Exception("Flight not found with id " + id));

        if (flightRequest.getFlightNumber() != null &&
                flightRepository.existsByFlightNumberAndIdNot(flightRequest.getFlightNumber(), id)) {
            throw new Exception("Flight number already exists");
        }

        FlightMapper.updateEntity(flightRequest, existing);
        Flight updated = flightRepository.save(existing);
        return convertToFlightResponse(updated);
    }

    @Override
    public FlightResponse changeStatus(Long id, FlightStatus status, String roles) throws Exception {
        Flight existing = flightRepository.findById(id)
                .orElseThrow(() -> new Exception("Flight not found with id " + id));
        existing.setStatus(status);
        Flight updated = flightRepository.save(existing);
        return convertToFlightResponse(updated);
    }


    @Override
    public void deleteFlight(Long airlineId, Long id, String roles) throws Exception {
        Flight existing = flightRepository.findById(id)
                .orElseThrow(() -> new Exception("Flight not found with id " + id));
        flightRepository.delete(existing);
    }

    private FlightResponse convertToFlightResponse(Flight flight) {
        AircraftResponse aircraft = null;
        try {
            aircraft = airlineClient.getAircraftById(flight.getAircraftId());
        } catch (Exception e) {
            aircraft = new AircraftResponse();
            aircraft.setId(flight.getAircraftId());
            aircraft.setCode("N/A");
            aircraft.setModel("Unknown Aircraft");
        }

        AirlineResponse airline = null;
        try {
            airline = airlineClient.getAirlineById(flight.getAirlineId());
        } catch (Exception e) {
            airline = new AirlineResponse();
            airline.setId(flight.getAirlineId());
            airline.setName("Unknown Airline");
        }

        AirportResponse departureAirport = null;
        try {
            departureAirport = locationClient.getAirportById(flight.getDepartureAirportId());
        } catch (Exception e) {
            departureAirport = new AirportResponse();
            departureAirport.setId(flight.getDepartureAirportId());
            departureAirport.setIataCode("UNK");
            com.zosh.payload.response.CityResponse city = new com.zosh.payload.response.CityResponse();
            city.setName("Unknown City");
            departureAirport.setCity(city);
        }

        AirportResponse arrivalAirport = null;
        try {
            arrivalAirport = locationClient.getAirportById(flight.getArrivalAirportId());
        } catch (Exception e) {
            arrivalAirport = new AirportResponse();
            arrivalAirport.setId(flight.getArrivalAirportId());
            arrivalAirport.setIataCode("UNK");
            com.zosh.payload.response.CityResponse city = new com.zosh.payload.response.CityResponse();
            city.setName("Unknown City");
            arrivalAirport.setCity(city);
        }

        return FlightMapper.toResponse(flight, aircraft, airline, departureAirport, arrivalAirport);
    }
}
