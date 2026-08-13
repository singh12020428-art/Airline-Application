package com.zosh.service;

import com.zosh.payload.request.AircraftRequest;
import com.zosh.payload.response.AircraftResponse;

import java.util.List;

public interface AircraftService {

    AircraftResponse createAircraft(AircraftRequest request, Long ownerId) throws Exception;

    AircraftResponse getAircraftById(Long id) throws Exception;

    List<AircraftResponse> listAllAircraftByOwner(Long ownerId, String roles) throws Exception;

    AircraftResponse updateAircraft(Long id, AircraftRequest request, Long ownerId, String roles) throws Exception;

    void deleteAircraft(Long id, Long ownerId, String roles) throws Exception;
}