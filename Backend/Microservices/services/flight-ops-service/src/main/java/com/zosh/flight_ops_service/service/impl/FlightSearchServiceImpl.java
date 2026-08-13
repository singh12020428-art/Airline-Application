package com.zosh.flight_ops_service.service.impl;

import com.zosh.flight_ops_service.client.AirlineClient;
import com.zosh.flight_ops_service.client.LocationClient;
import com.zosh.flight_ops_service.client.PricingClient;
import com.zosh.flight_ops_service.mapper.FlightInstanceMapper;
import com.zosh.flight_ops_service.model.FlightInstance;
import com.zosh.flight_ops_service.repository.FlightInstanceRepository;
import com.zosh.flight_ops_service.service.FlightSearchService;
import com.zosh.flight_ops_service.service.specification.FlightInstanceSpecification;
import com.zosh.payload.request.FlightSearchRequest;
import com.zosh.payload.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.JpaSort;
import com.zosh.flight_ops_service.client.SeatClient;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FlightSearchServiceImpl implements FlightSearchService {

    private final FlightInstanceRepository flightInstanceRepository;
    private final PricingClient pricingClient;
    private final SeatClient seatClient;
    private final AirlineClient airlineClient;
    private final LocationClient locationClient;

    @Override
    public Page<FlightInstanceResponse> searchFlights(FlightSearchRequest request, Pageable pageable) {
        Pageable sortedPageable = applySort(pageable, request.getSortBy(), request.getSortOrder());

        Specification<FlightInstance> specification =
                FlightInstanceSpecification.buildSearchSpec(request);

        Page<FlightInstance> dbPage = flightInstanceRepository.findAll(specification, sortedPageable);

        if (dbPage.isEmpty()) {
            return Page.empty(sortedPageable);
        }

        List<FlightInstance> instances = new ArrayList<>(dbPage.getContent());
        Map<Long, FareResponse> fareMap = Collections.emptyMap();

        // Cabin class + price filtering
        if (request.getCabinClass() != null) {
            final boolean hasPriceFilter = request.getMinPrice() != null && request.getMaxPrice() != null;
            Map<Long, FareResponse> mergedFareMap = new HashMap<>();
            List<FlightInstance> filtered = new ArrayList<>();

            for (FlightInstance fi : instances) {
                CabinClassResponse cabinClassResponse = seatClient.getCabinClassByAircraftIdAndName(
                        request.getCabinClass(),
                        fi.getFlight().getAircraftId()
                );

                Long cabinClassId = cabinClassResponse != null ? cabinClassResponse.getId() : null;
                if (cabinClassId == null) continue;

                FareResponse fare = pricingClient.getLowestFareForFlightAndCabinClass(
                        fi.getFlight().getId(),
                        cabinClassId
                );

                if (hasPriceFilter) {
                    if (fare == null) continue;
                    Double price = fare.getCurrentPrice();
                    if (price == null) continue;
                    if (price < request.getMinPrice()) continue;
                    if (price > request.getMaxPrice()) continue;
                }

                if (fare != null) {
                    mergedFareMap.put(fi.getFlight().getId(), fare);
                }
                filtered.add(fi);
            }

            fareMap = mergedFareMap;
            instances = filtered;
        }

        if (instances.isEmpty()) {
            return Page.empty(sortedPageable);
        }

        List<FlightInstanceResponse> responses = enrichWithExternalData(instances, fareMap);

        return new PageImpl<>(responses, sortedPageable, dbPage.getTotalElements());
    }

    private Pageable applySort(Pageable pageable, String sortBy, String sortOrder) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;

        Sort sort = (sortBy == null || sortBy.isBlank())
                ? Sort.by(direction, "departureDateTime")
                : switch (sortBy.toLowerCase()) {
            case "arrival" -> Sort.by(direction, "arrivalDateTime");
            case "duration" -> JpaSort.unsafe(direction, "(arrivalDateTime - departureDateTime)");
            default -> Sort.by(direction, "departureDateTime");
        };

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private List<FlightInstanceResponse> enrichWithExternalData(
            List<FlightInstance> instances,
            Map<Long, FareResponse> fareMap) {

        Map<Long, AirlineResponse> airlineCache = new HashMap<>();
        Map<Long, AirportResponse> airportCache = new HashMap<>();
        Map<Long, AircraftResponse> aircraftCache = new HashMap<>();

        List<FlightInstanceResponse> results = new ArrayList<>(instances.size());

        for (FlightInstance fi : instances) {
            AircraftResponse aircraft = aircraftCache.computeIfAbsent(
                    fi.getFlight().getAircraftId(),
                    airlineClient::getAircraftById
            );

            AirlineResponse airline = airlineCache.computeIfAbsent(
                    fi.getAirlineId(),
                    airlineClient::getAirlineById
            );

            AirportResponse depAirport = airportCache.computeIfAbsent(
                    fi.getDepartureAirportId(),
                    locationClient::getAirportById
            );

            AirportResponse arrAirport = airportCache.computeIfAbsent(
                    fi.getArrivalAirportId(),
                    locationClient::getAirportById
            );

            FlightInstanceResponse response = FlightInstanceMapper.toResponse(
                    fi, aircraft, airline, depAirport, arrAirport
            );

            response.setFareResponse(fareMap.get(fi.getFlight().getId()));
            results.add(response);
        }

        return results;
    }

    @Override
    public com.zosh.event.FlightStatusUpdatedEvent getFlightStatus(String flightNumber, java.time.LocalDate date) {
        java.time.LocalDateTime startOfDay = date.atStartOfDay();
        java.time.LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        java.util.List<FlightInstance> instances = flightInstanceRepository.findByFlightNumberAndDate(flightNumber, startOfDay, endOfDay);
        if (instances.isEmpty()) {
            throw new RuntimeException("Flight not found");
        }
        FlightInstance fi = instances.get(0);

        AirportResponse depAirport = locationClient.getAirportById(fi.getDepartureAirportId());
        AirportResponse arrAirport = locationClient.getAirportById(fi.getArrivalAirportId());
        AirlineResponse airline = airlineClient.getAirlineById(fi.getAirlineId());

        return com.zosh.event.FlightStatusUpdatedEvent.builder()
                .flightNumber(fi.getFlight().getFlightNumber())
                .airline(airline.getName())
                .fromCode(depAirport.getIataCode())
                .fromCity(depAirport.getCity().getName())
                .toCode(arrAirport.getIataCode())
                .toCity(arrAirport.getCity().getName())
                .status(fi.getStatus())
                .scheduledDeparture(fi.getDepartureDateTime())
                .estimatedDeparture(fi.getEstimatedDeparture())
                .scheduledArrival(fi.getArrivalDateTime())
                .estimatedArrival(fi.getEstimatedArrival())
                .terminal(fi.getTerminal() != null ? fi.getTerminal() : "T1")
                .gate(fi.getGate() != null ? fi.getGate() : "TBD")
                .belt(fi.getBelt() != null ? fi.getBelt() : "TBD")
                .delayDuration(fi.getDelayDuration())
                .build();
    }
    @Override
    public java.util.List<com.zosh.event.FlightStatusUpdatedEvent> getAllFlightStatuses(java.time.LocalDate date) {
        java.time.LocalDateTime startOfDay = date.atStartOfDay();
        java.time.LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        java.util.List<FlightInstance> instances = flightInstanceRepository.findAllByDate(startOfDay, endOfDay);
        java.util.List<com.zosh.event.FlightStatusUpdatedEvent> events = new ArrayList<>();
        
        // Deduplicate by flight number, keeping the most recently updated one
        java.util.Map<String, FlightInstance> uniqueInstances = new java.util.HashMap<>();
        for (FlightInstance fi : instances) {
            String fNum = fi.getFlight().getFlightNumber();
            if (!uniqueInstances.containsKey(fNum) || 
                (fi.getUpdatedAt() != null && uniqueInstances.get(fNum).getUpdatedAt() != null && 
                 fi.getUpdatedAt().isAfter(uniqueInstances.get(fNum).getUpdatedAt()))) {
                uniqueInstances.put(fNum, fi);
            }
        }

        java.util.Map<Long, AirportResponse> airportCache = new java.util.HashMap<>();
        java.util.Map<Long, AirlineResponse> airlineCache = new java.util.HashMap<>();

        for (FlightInstance fi : uniqueInstances.values()) {
            AirportResponse depAirport = airportCache.computeIfAbsent(fi.getDepartureAirportId(), locationClient::getAirportById);
            AirportResponse arrAirport = airportCache.computeIfAbsent(fi.getArrivalAirportId(), locationClient::getAirportById);
            AirlineResponse airline = airlineCache.computeIfAbsent(fi.getAirlineId(), airlineClient::getAirlineById);

            events.add(com.zosh.event.FlightStatusUpdatedEvent.builder()
                    .flightNumber(fi.getFlight().getFlightNumber())
                    .airline(airline.getName())
                    .fromCode(depAirport.getIataCode())
                    .fromCity(depAirport.getCity().getName())
                    .toCode(arrAirport.getIataCode())
                    .toCity(arrAirport.getCity().getName())
                    .status(fi.getStatus())
                    .scheduledDeparture(fi.getDepartureDateTime())
                    .estimatedDeparture(fi.getEstimatedDeparture())
                    .scheduledArrival(fi.getArrivalDateTime())
                    .estimatedArrival(fi.getEstimatedArrival())
                    .terminal(fi.getTerminal() != null ? fi.getTerminal() : "T1")
                    .gate(fi.getGate() != null ? fi.getGate() : "TBD")
                    .belt(fi.getBelt() != null ? fi.getBelt() : "TBD")
                    .delayDuration(fi.getDelayDuration())
                    .build());
        }

        return events;
    }
}
