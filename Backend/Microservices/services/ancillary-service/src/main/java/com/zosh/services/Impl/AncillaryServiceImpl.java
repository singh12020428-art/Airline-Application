package com.zosh.services.Impl;

import com.zosh.config.AirlineClient;
import com.zosh.mapper.AncillaryMapper;
import com.zosh.mapper.InsuranceCoverageMapper;
import com.zosh.model.Ancillary;
import com.zosh.model.InsuranceCoverage;
import com.zosh.payload.request.AncillaryRequest;
import com.zosh.payload.response.AirlineResponse;
import com.zosh.payload.response.AncillaryResponse;
import com.zosh.payload.response.InsuranceCoverageResponse;
import com.zosh.repository.AncillaryRepository;
import com.zosh.repository.InsuranceCoverageRepository;
import com.zosh.services.AncillaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AncillaryServiceImpl implements AncillaryService {

    private final AncillaryRepository ancillaryRepository;
    private final InsuranceCoverageRepository coverageRepository;
    private final AirlineClient airlineClient;

    @Override
    public AncillaryResponse createAncillary(Long userId, AncillaryRequest request) {

        AirlineResponse airlineResponse = airlineClient.getAirlineByOwner(userId);

        Ancillary ancillary = Ancillary.builder()
                .type(request.getType())
                .subType(request.getSubType())
                .rfisc(request.getRfisc())
                .name(request.getName())
                .description(request.getDescription())
                .iconUrl(request.getIconUrl())
                .metadata(request.getMetadata())
                .displayOrder(request.getDisplayOrder())
                .airlineId(airlineResponse.getId())
                .build();

        Ancillary saved = ancillaryRepository.save(ancillary);
        return AncillaryMapper.toResponse(saved, null);
    }

    @Override
    public AncillaryResponse getById(Long id) throws Exception {
        Ancillary ancillary = ancillaryRepository.findById(id)
                .orElseThrow(() -> new Exception("Ancillary not found"));

        // fetch insurance coverages by ancillary
        List<InsuranceCoverage> coverages = coverageRepository.findByAncillaryId(ancillary.getId());
        List<InsuranceCoverageResponse> coverageResponses = coverages.stream()
                .map(InsuranceCoverageMapper::toResponse).toList();
        return AncillaryMapper.toResponse(ancillary, coverageResponses);
    }

    @Override
    public List<AncillaryResponse> getByAirlineId(Long userId, String roles) {

        List<Ancillary> ancillaries;
        if (roles != null && roles.contains("ROLE_SYSTEM_ADMIN")) {
            ancillaries = ancillaryRepository.findAll();
        } else {
            AirlineResponse airlineResponse = airlineClient.getAirlineByOwner(userId);
            ancillaries = ancillaryRepository.findByAirlineId(airlineResponse.getId());
        }

        return ancillaries.stream()
                .map(ancillary -> {

                    // fetch insurance coverage by ancillary
                            List<InsuranceCoverage> coverages = coverageRepository.findByAncillaryId(ancillary.getId());
                            List<InsuranceCoverageResponse> coverageResponses = coverages.stream()
                                    .map(InsuranceCoverageMapper::toResponse).toList();
                            return  AncillaryMapper.toResponse(ancillary, coverageResponses);
                        }).collect(Collectors.toList());
    }

    @Override
    public AncillaryResponse updateAncillary(Long id, AncillaryRequest request) throws Exception {
        Ancillary ancillary = ancillaryRepository.findById(id)
                .orElseThrow(() -> new Exception("Ancillary not found"));

        ancillary.setType(request.getType());
        ancillary.setSubType(request.getSubType());
        ancillary.setRfisc(request.getRfisc());
        ancillary.setName(request.getName());
        ancillary.setDescription(request.getDescription());
        ancillary.setIconUrl(request.getIconUrl());
        ancillary.setMetadata(request.getMetadata());
        ancillary.setDisplayOrder(request.getDisplayOrder());

        Ancillary updated = ancillaryRepository.save(ancillary);

        // fetch insurance coverages by ancillary
        List<InsuranceCoverage> coverages = coverageRepository.findByAncillaryId(ancillary.getId());
        List<InsuranceCoverageResponse> coverageResponses = coverages.stream()
                .map(InsuranceCoverageMapper::toResponse).toList();
        return AncillaryMapper.toResponse(updated
                , coverageResponses);
    }

    @Override
    public void deleteAncillary(Long id) throws Exception {
        Ancillary ancillary = ancillaryRepository.findById(id)
                .orElseThrow(() -> new Exception("Ancillary not found"));
        ancillaryRepository.delete(ancillary);
    }
}
