package com.zosh.services;

import com.zosh.payload.request.AncillaryRequest;
import com.zosh.payload.response.AncillaryResponse;

import java.util.List;

public interface AncillaryService {
    AncillaryResponse createAncillary(Long userId, AncillaryRequest request);
    AncillaryResponse getById(Long id) throws Exception;
    List<AncillaryResponse> getByAirlineId(Long userId, String roles);
    AncillaryResponse updateAncillary(Long id, AncillaryRequest request) throws Exception;
    void deleteAncillary(Long id) throws Exception;
}
