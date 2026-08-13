package com.zosh.flight_ops_service.mapper;

import com.zosh.enums.FlightStatus;
import com.zosh.flight_ops_service.model.Flight;
import com.zosh.flight_ops_service.model.FlightInstance;
import com.zosh.payload.request.FlightInstanceRequest;
import com.zosh.payload.response.AircraftResponse;
import com.zosh.payload.response.AirlineResponse;
import com.zosh.payload.response.AirportResponse;
import com.zosh.payload.response.FlightInstanceResponse;

public class FlightInstanceMapper {

    public static FlightInstance toEntity(FlightInstanceRequest request, Flight flight) {
        if (request == null || flight == null) return null;

        return FlightInstance.builder()
                .flight(flight)
                .airlineId(flight.getAirlineId())
                .scheduleId(request.getScheduleId() != null ? request.getScheduleId() : -1L)
                .departureAirportId(request.getDepartureAirportId() != null ? request.getDepartureAirportId() : flight.getDepartureAirportId())
                .arrivalAirportId(request.getArrivalAirportId() != null ? request.getArrivalAirportId() : flight.getArrivalAirportId())
                .departureDateTime(request.getDepartureDateTime())
                .arrivalDateTime(request.getArrivalDateTime())
                .status(FlightStatus.SCHEDULED)
                .minAdvanceBookingDays(request.getMinAdvanceBookingDays())
                .maxAdvanceBookingDays(request.getMaxAdvanceBookingDays())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .totalSeats(request.getTotalSeats())
                .availableSeats(request.getAvailableSeats())
                .estimatedDeparture(request.getEstimatedDeparture())
                .estimatedArrival(request.getEstimatedArrival())
                .terminal(request.getTerminal())
                .gate(request.getGate())
                .belt(request.getBelt())
                .delayDuration(request.getDelayDuration())
                .build();
    }

    public static FlightInstanceResponse toResponse(FlightInstance fi,
                                                    AircraftResponse aircraftResponse,
                                                    AirlineResponse airline,
                                                    AirportResponse departureAirport,
                                                    AirportResponse arrivalAirport) {
        if (fi == null) return null;

        return FlightInstanceResponse.builder()
                .id(fi.getId())
                .flightId(fi.getFlight() != null ? fi.getFlight().getId() : null)
                .flightNumber(fi.getFlight() != null ? fi.getFlight().getFlightNumber() : null)
                .aircraftId(fi.getFlight() != null ? fi.getFlight().getAircraftId() : null)
                .aircraftModel(aircraftResponse != null ? aircraftResponse.getModel() : null)
                .aircraftCode(aircraftResponse != null ? aircraftResponse.getCode() : null)
                .airlineId(fi.getAirlineId())
                .airlineName(airline != null ? airline.getName() : null)
                .airlineLogo(airline != null ? airline.getLogoUrl() : null)
                .departureAirport(departureAirport)
                .arrivalAirport(arrivalAirport)
                .departureDateTime(fi.getDepartureDateTime())
                .arrivalDateTime(fi.getArrivalDateTime())
                .formattedDuration(fi.getFormattedDuration())
                .totalSeats(fi.getTotalSeats())
                .availableSeats(fi.getAvailableSeats())
                .status(fi.getStatus())
                .minAdvanceBookingDays(fi.getMinAdvanceBookingDays())
                .maxAdvanceBookingDays(fi.getMaxAdvanceBookingDays())
                .isActive(fi.getIsActive())
                .estimatedDeparture(fi.getEstimatedDeparture())
                .estimatedArrival(fi.getEstimatedArrival())
                .terminal(fi.getTerminal())
                .gate(fi.getGate())
                .belt(fi.getBelt())
                .delayDuration(fi.getDelayDuration())
                .build();
    }

    public static void updateEntity(FlightInstanceRequest request, FlightInstance existing) {
        if (request == null || existing == null) return;

        if (request.getDepartureAirportId() != null) existing.setDepartureAirportId(request.getDepartureAirportId());
        if (request.getArrivalAirportId() != null) existing.setArrivalAirportId(request.getArrivalAirportId());
        if (request.getDepartureDateTime() != null) existing.setDepartureDateTime(request.getDepartureDateTime());
        if (request.getArrivalDateTime() != null) existing.setArrivalDateTime(request.getArrivalDateTime());if (request.getTotalSeats() != null) existing.setTotalSeats(request.getTotalSeats());
        if (request.getAvailableSeats() != null) existing.setAvailableSeats(request.getAvailableSeats());
        if (request.getStatus() != null) existing.setStatus(request.getStatus());
        if (request.getMinAdvanceBookingDays() != null) existing.setMinAdvanceBookingDays(request.getMinAdvanceBookingDays());
        if (request.getMaxAdvanceBookingDays() != null) existing.setMaxAdvanceBookingDays(request.getMaxAdvanceBookingDays());
        if (request.getIsActive() != null) existing.setIsActive(request.getIsActive());

        if (request.getEstimatedDeparture() != null) existing.setEstimatedDeparture(request.getEstimatedDeparture());
        if (request.getEstimatedArrival() != null) existing.setEstimatedArrival(request.getEstimatedArrival());
        if (request.getTerminal() != null) existing.setTerminal(request.getTerminal());
        if (request.getGate() != null) existing.setGate(request.getGate());
        if (request.getBelt() != null) existing.setBelt(request.getBelt());
        if (request.getDelayDuration() != null) existing.setDelayDuration(request.getDelayDuration());
    }
}
