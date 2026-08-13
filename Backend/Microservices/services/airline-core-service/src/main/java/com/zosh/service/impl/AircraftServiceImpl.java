package com.zosh.service.impl;

import com.zosh.mapper.AircraftMapper;
import com.zosh.model.Aircraft;
import com.zosh.model.Airline;
import com.zosh.payload.request.AircraftRequest;
import com.zosh.payload.response.AircraftResponse;
import com.zosh.repository.AircraftRepository;
import com.zosh.repository.AirlineRepository;
import com.zosh.service.AircraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AircraftServiceImpl implements AircraftService {

    private final AircraftRepository aircraftRepository;
    private final AirlineRepository airlineRepository;

    @Override
    public AircraftResponse createAircraft(AircraftRequest request, Long ownerId) throws Exception {
        Airline airline = airlineRepository.findFirstByOwnerId(ownerId)
                .orElseThrow(() -> new Exception("Airline not exist for this ownerId"));

        Aircraft aircraft = AircraftMapper.toEntity(request, airline);

        if (aircraftRepository.existsByCode(aircraft.getCode())) {
            throw new Exception("Code already exists with another aircraft");
        }

        if (aircraft.getSeatingCapacity() < aircraft.getTotalSeats()) {
            throw new Exception("Seating capacity can't be less than total seats");
        }

        return AircraftMapper.toResponse(aircraftRepository.save(aircraft));
    }

    @Override
    public AircraftResponse getAircraftById(Long id) throws Exception {
        Aircraft aircraft = aircraftRepository.findById(id)
                .orElseThrow(() -> new Exception("Aircraft not exist with id"));
        return AircraftMapper.toResponse(aircraft);
    }


    @Override
    public List<AircraftResponse> listAllAircraftByOwner(Long ownerId, String roles) throws Exception {
        if (roles != null && roles.contains("ROLE_SYSTEM_ADMIN")) {
            return aircraftRepository.findAll()
                    .stream()
                    .map(AircraftMapper::toResponse)
                    .toList();
        }

        Airline airline = airlineRepository.findFirstByOwnerId(ownerId)
                .orElseThrow(() -> new Exception("This owner doesn't have an airline"));

        return aircraftRepository.findByAirlineId(airline.getId())
                .stream()
                .map(AircraftMapper::toResponse)
                .toList();
    }

    @Override
    public AircraftResponse updateAircraft(Long id, AircraftRequest request, Long ownerId, String roles) throws Exception {
        Aircraft aircraft;
        if (roles != null && roles.contains("ROLE_SYSTEM_ADMIN")) {
            aircraft = aircraftRepository.findById(id).orElse(null);
        } else {
            Airline airline = airlineRepository.findFirstByOwnerId(ownerId)
                    .orElseThrow(() -> new Exception("This owner doesn't have an airline"));
            aircraft = aircraftRepository.findByIdAndAirlineId(id, airline.getId());
        }

        if (aircraft == null) {
            throw new Exception("Aircraft not exist with id");
        }

        if (request.getCode() != null
                && !aircraft.getCode().equals(request.getCode())
                && aircraftRepository.existsByCode(request.getCode())) {
            throw new Exception("Code already exists with another aircraft");
        }

        AircraftMapper.updateEntity(aircraft, request);

        if (aircraft.getSeatingCapacity() < aircraft.getTotalSeats()) {
            throw new Exception("Seating capacity can't be less than total seats");
        }

        return AircraftMapper.toResponse(aircraftRepository.save(aircraft));
    }

    @Override
    public void deleteAircraft(Long id, Long ownerId, String roles) throws Exception {
        Aircraft aircraft;
        if (roles != null && roles.contains("ROLE_SYSTEM_ADMIN")) {
            aircraft = aircraftRepository.findById(id).orElse(null);
        } else {
            Airline airline = airlineRepository.findFirstByOwnerId(ownerId)
                    .orElseThrow(() -> new Exception("This owner doesn't have an airline"));
            aircraft = aircraftRepository.findByIdAndAirlineId(id, airline.getId());
        }

        if (aircraft == null) {
            throw new Exception("Aircraft not exist with id");
        }

        aircraftRepository.delete(aircraft);
    }
}
