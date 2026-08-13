package com.zosh.service.impl;

import com.zosh.enums.AirlineStatus;
import com.zosh.mapper.AirlineMapper;
import com.zosh.model.Airline;
import com.zosh.payload.request.AirlineRequest;
import com.zosh.payload.response.AirlineDropdownItem;
import com.zosh.payload.response.AirlineResponse;
import com.zosh.repository.AirlineRepository;
import com.zosh.service.AirlineService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import com.zosh.client.LocationClient;
import com.zosh.client.UserClient;
import com.zosh.payload.response.CityResponse;
import com.zosh.payload.dto.UserDTO;

@Service
public class AirlineServiceImpl implements AirlineService {

    private final AirlineRepository airlineRepository;
    private final LocationClient locationClient;
    private final UserClient userClient;

    public AirlineServiceImpl(AirlineRepository airlineRepository, LocationClient locationClient, UserClient userClient) {
        this.airlineRepository = airlineRepository;
        this.locationClient = locationClient;
        this.userClient = userClient;
    }

    private AirlineResponse mapToResponse(Airline airline) {
        AirlineResponse response = AirlineMapper.toResponse(airline);
        if (response != null) {
            try {
                if (airline.getHeadquarterCityId() != null) {
                    CityResponse city = locationClient.getCityById(airline.getHeadquarterCityId());
                    response.setHeadquartersCity(city);
                }
                if (airline.getOwnerId() != null) {
                    UserDTO owner = userClient.getUserById(airline.getOwnerId());
                    response.setOwner(owner);
                }
                if (airline.getUpdatedById() != null) {
                    // updatedBy can be fetched too, but let's just populate owner for now, or populate it if needed
                    // response.setUpdatedById(airline.getUpdatedById());
                }
            } catch (Exception e) {
                // Ignore client failures, just return the basic response
            }
        }
        return response;
    }

    @Override
    public AirlineResponse createAirline(AirlineRequest request, Long ownerId) {
        // Prevent duplicate airlines
        if (airlineRepository.findFirstByOwnerId(ownerId).isPresent()) {
            throw new RuntimeException("An airline already exists for this owner.");
        }

        Airline airline = AirlineMapper.toEntity(request, ownerId);
        // CRITICAL: Force new airlines to be PENDING so the System Admin must approve them
        airline.setStatus(AirlineStatus.PENDING);
        
        Airline savedAirline = airlineRepository.save(airline);
        return mapToResponse(savedAirline);
    }

    @Override
    public AirlineResponse getAirlineByOwner(Long ownerId) throws Exception {
        Airline airline = airlineRepository.findFirstByOwnerId(ownerId)
                .orElseThrow(
                        ()->new Exception("Airline not found with ownerId" +ownerId)
                );

        return mapToResponse(airline);
    }

    @Override
    public AirlineResponse getAirlineById(Long id) throws Exception {
        Airline airline = airlineRepository.findById(id)
                .orElseThrow(
                        ()->new Exception("Airline not found with ownerId" +id)
                );

        return mapToResponse(airline);
    }

    @Override
    public Page<AirlineResponse> getAllAirlines(Pageable pageable) {
        return airlineRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public AirlineResponse updateAirlines(AirlineRequest request, Long ownerId) throws Exception {
        Airline airline = airlineRepository.findFirstByOwnerId(ownerId)
                .orElseThrow(
                        ()->new Exception("Airline not found with ownerId" +ownerId)
                );
        AirlineMapper.updateEntity(airline, request);
        Airline savedAirline = airlineRepository.save(airline);
        return mapToResponse(savedAirline);
    }

    @Override
    public void deleteAirline(Long id, Long ownerId) throws Exception {
        Airline airline = airlineRepository.findFirstByOwnerId(ownerId)
                .orElseThrow(
                        ()->new Exception("Airline not found with ownerId" +ownerId)
                );

        airlineRepository.delete(airline);
    }

    @Override
    public AirlineResponse changeStatusByAdmin(Long airlineId, AirlineStatus status) throws Exception {
        Airline airline = airlineRepository.findById(airlineId)
                .orElseThrow(
                        ()->new Exception("Airline not found with ownerId" +airlineId)
                );
        airline.setStatus(status);
        Airline updatedAirline = airlineRepository.save(airline);
        return mapToResponse(updatedAirline);
    }

    @Override
    public List<AirlineDropdownItem> getAirlineDropdown() {
        return airlineRepository.findByStatus(AirlineStatus.ACTIVE)
                .stream()
                .map(a-> AirlineDropdownItem.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .iataCode(a.getIataCode())
                        .icaoCode(a.getIcaoCode())
                        .logoUrl(a.getLogoUrl())
                        .build()).toList();
    }
}
