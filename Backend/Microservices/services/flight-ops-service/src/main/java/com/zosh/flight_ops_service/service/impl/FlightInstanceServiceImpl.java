package com.zosh.flight_ops_service.service.impl;

import com.zosh.event.FlightInstanceCreatedEvent;
import com.zosh.flight_ops_service.client.AirlineClient;
import com.zosh.flight_ops_service.client.LocationClient;
import com.zosh.flight_ops_service.event.FlightInstanceEventProducer;
import com.zosh.flight_ops_service.mapper.FlightInstanceMapper;
import com.zosh.flight_ops_service.model.Flight;
import com.zosh.flight_ops_service.model.FlightInstance;
import com.zosh.payload.request.FlightInstanceRequest;
import com.zosh.payload.response.AircraftResponse;
import com.zosh.payload.response.AirlineResponse;
import com.zosh.payload.response.AirportResponse;
import com.zosh.payload.response.FlightInstanceResponse;
import com.zosh.flight_ops_service.repository.FlightInstanceRepository;
import com.zosh.flight_ops_service.repository.FlightRepository;
import com.zosh.flight_ops_service.service.FlightInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FlightInstanceServiceImpl implements FlightInstanceService {

    private final FlightInstanceRepository flightInstanceRepository;
    private final FlightRepository flightRepository;
    private final AirlineClient airlineClient;
    private final LocationClient  locationClient;
    private final FlightInstanceEventProducer flightInstanceEventProducer;
    private final org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public FlightInstanceResponse createFlightInstance(Long userId, FlightInstanceRequest request) throws Exception {

        AirlineResponse airlineResponse;
        try {
            airlineResponse = airlineClient.getAirlineByOwner(userId);
        } catch (feign.FeignException e) {
            throw new Exception("You must create an Airline profile first before you can create a flight instance.");
        }

        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new Exception("Flight not found with id " + request.getFlightId()));

        // Example aircraft placeholder (replace with actual lookup if needed)
        AircraftResponse aircraft = airlineClient.getAircraftById(flight.getAircraftId());

        FlightInstance flightInstance = FlightInstanceMapper.toEntity(request, flight);
        flightInstance.setTotalSeats(aircraft.getTotalSeats());
        flightInstance.setAvailableSeats(aircraft.getTotalSeats());

        FlightInstance saved = flightInstanceRepository.save(flightInstance);

        // TODO: create seat instances if required
        FlightInstanceCreatedEvent event = FlightInstanceCreatedEvent.builder()
                .flightInstanceId(flightInstance.getId())
                .aircraftId(flight.getAircraftId())
                .flightId(flight.getId())
                .build();
        flightInstanceEventProducer.sendFlightInstanceCreated(event);
        // publish kafka event, seat service consume that can create seat instance
        return convertToFlightInstanceResponse(saved);
    }

    @Override
    public FlightInstanceResponse getFlightInstanceById(Long id) throws Exception {
        FlightInstance flightInstance = flightInstanceRepository.findById(id)
                .orElseThrow(() -> new Exception("Flight instance not found with id " + id));
        return convertToFlightInstanceResponse(flightInstance);
    }
    @Override
    public Page<FlightInstanceResponse> getByAirlineId(Long userId, String roles,
                                                       Long departureAirportId,
                                                       Long arrivalAirportId,
                                                       Long flightId,
                                                       LocalDate onDate,
                                                       Pageable pageable) {

        LocalDateTime start = null;
        LocalDateTime end = null;
        if (onDate != null) {
            start = onDate.atStartOfDay();
            end = onDate.plusDays(1).atStartOfDay();
        }

        if (roles != null && roles.contains("ROLE_SYSTEM_ADMIN")) {
            return flightInstanceRepository.findAllInstances(
                    departureAirportId, arrivalAirportId, flightId, start, end, pageable
            ).map(this::convertToFlightInstanceResponse);
        }

        AirlineResponse airlineResponse;
        try {
            airlineResponse = airlineClient.getAirlineByOwner(userId);
        } catch (feign.FeignException e) {
            throw new IllegalArgumentException("You must create an Airline profile first.");
        }

        return flightInstanceRepository.findByAirlineId(
                airlineResponse.getId(), departureAirportId, arrivalAirportId, flightId, start, end, pageable
        ).map(this::convertToFlightInstanceResponse);
    }


    @Override
    public FlightInstanceResponse updateFlightInstance(Long id, FlightInstanceRequest request) throws Exception {
        FlightInstance existing = flightInstanceRepository.findById(id)
                .orElseThrow(() -> new Exception("Flight instance not found with id " + id));

        FlightInstanceMapper.updateEntity(request, existing);
        FlightInstance updated = flightInstanceRepository.save(existing);
        
        AirportResponse depAirport = locationClient.getAirportById(updated.getDepartureAirportId());
        AirportResponse arrAirport = locationClient.getAirportById(updated.getArrivalAirportId());
        AirlineResponse airline = airlineClient.getAirlineById(updated.getAirlineId());

        // Fire FlightStatusUpdatedEvent with live data
        com.zosh.event.FlightStatusUpdatedEvent statusEvent = com.zosh.event.FlightStatusUpdatedEvent.builder()
                .flightNumber(updated.getFlight().getFlightNumber())
                .airline(airline.getName())
                .fromCode(depAirport.getIataCode())
                .fromCity(depAirport.getCity().getName())
                .toCode(arrAirport.getIataCode())
                .toCity(arrAirport.getCity().getName())
                .status(updated.getStatus())
                .scheduledDeparture(updated.getDepartureDateTime())
                .estimatedDeparture(updated.getEstimatedDeparture() != null ? updated.getEstimatedDeparture() : updated.getDepartureDateTime())
                .scheduledArrival(updated.getArrivalDateTime())
                .estimatedArrival(updated.getEstimatedArrival() != null ? updated.getEstimatedArrival() : updated.getArrivalDateTime())
                .terminal(updated.getTerminal() != null ? updated.getTerminal() : "TBD")
                .gate(updated.getGate() != null ? updated.getGate() : "TBD")
                .belt(updated.getBelt() != null ? updated.getBelt() : "-")
                .delayDuration(updated.getDelayDuration())
                .build();
                
        kafkaTemplate.send("flight-status-events", statusEvent);
        
        return convertToFlightInstanceResponse(updated);
    }

    @Override
    public void deleteFlightInstance(Long id) throws Exception {
        FlightInstance existing = flightInstanceRepository.findById(id)
                .orElseThrow(() -> new Exception("Flight instance not found with id " + id));
        flightInstanceRepository.delete(existing);
    }

    private FlightInstanceResponse convertToFlightInstanceResponse(FlightInstance flightInstance) {
        AirlineResponse airline = null;
        try {
            airline = airlineClient.getAirlineById(flightInstance.getAirlineId());
        } catch (Exception e) {
            airline = new AirlineResponse();
            airline.setId(flightInstance.getAirlineId());
            airline.setName("Unknown Airline");
        }

        AirportResponse departureAirport = null;
        try {
            departureAirport = locationClient.getAirportById(flightInstance.getDepartureAirportId());
        } catch (Exception e) {
            departureAirport = new AirportResponse();
            departureAirport.setId(flightInstance.getDepartureAirportId());
            departureAirport.setIataCode("UNK");
            com.zosh.payload.response.CityResponse city = new com.zosh.payload.response.CityResponse();
            city.setName("Unknown City");
            departureAirport.setCity(city);
        }

        AirportResponse arrivalAirport = null;
        try {
            arrivalAirport = locationClient.getAirportById(flightInstance.getArrivalAirportId());
        } catch (Exception e) {
            arrivalAirport = new AirportResponse();
            arrivalAirport.setId(flightInstance.getArrivalAirportId());
            arrivalAirport.setIataCode("UNK");
            com.zosh.payload.response.CityResponse city = new com.zosh.payload.response.CityResponse();
            city.setName("Unknown City");
            arrivalAirport.setCity(city);
        }

        AircraftResponse aircraftResponse = null;
        try {
            aircraftResponse = airlineClient.getAircraftById(flightInstance.getFlight().getAircraftId());
        } catch (Exception e) {
            aircraftResponse = new AircraftResponse();
            aircraftResponse.setId(flightInstance.getFlight().getAircraftId());
            aircraftResponse.setCode("N/A");
            aircraftResponse.setModel("Unknown Aircraft");
        }

        return FlightInstanceMapper.toResponse(
                flightInstance,
                aircraftResponse,
                airline,
                departureAirport,
                arrivalAirport
        );
    }
}
