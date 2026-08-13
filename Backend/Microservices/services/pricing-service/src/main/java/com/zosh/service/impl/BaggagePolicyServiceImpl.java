package com.zosh.service.impl;

import com.zosh.Mapper.BaggagePolicyMapper;
import com.zosh.model.BaggagePolicy;
import com.zosh.model.Fare;
import com.zosh.payload.request.BaggagePolicyRequest;
import com.zosh.payload.response.BaggagePolicyResponse;
import com.zosh.repository.BaggagePolicyRepository;
import com.zosh.repository.FareRepository;
import com.zosh.service.BaggagePolicyService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BaggagePolicyServiceImpl implements BaggagePolicyService {

    private final FareRepository fareRepository;
    private final BaggagePolicyRepository baggagePolicyRepository;

    @Override
    public BaggagePolicyResponse createBaggagePolicy(BaggagePolicyRequest request) throws Exception {
        Fare fare = fareRepository.findById(request.getFareId())
                .orElseThrow(
                        () -> new Exception("baggage policy not found")
                );
        if (baggagePolicyRepository.existsByFareId(fare.getId())) {
            throw new Exception("baggage policy already exists");
        }
        BaggagePolicy baggagePolicy = BaggagePolicyMapper.toEntity(request, fare);
        BaggagePolicy saved = baggagePolicyRepository.save(baggagePolicy);
        return BaggagePolicyMapper.toResponse(saved);
    }

    @Override
    public BaggagePolicyResponse getBaggagePolicyById(Long id) throws Exception {
        BaggagePolicy baggagePolicy = baggagePolicyRepository.findById(id)
                .orElseThrow(
                        () -> new Exception("policy not found with id")
                );
        return BaggagePolicyMapper.toResponse(baggagePolicy);
    }

    @Override
    public BaggagePolicyResponse getBaggagePolicyByFareId(Long fareId) {
        BaggagePolicy baggagePolicy = baggagePolicyRepository.findByFareId(fareId);
        return BaggagePolicyMapper.toResponse(baggagePolicy);
    }

    @Override
    public List<BaggagePolicyResponse> getBaggagePoliciesByAirlineId(Long airlineId) {
        return baggagePolicyRepository.findByAirlineId(airlineId)
                .stream().map(
                        BaggagePolicyMapper::toResponse
                ).collect(Collectors.toList());
    }

    @Override
    public BaggagePolicyResponse updateBaggagePolicy(Long id, BaggagePolicyRequest request) throws Exception {
        BaggagePolicy baggagePolicy = baggagePolicyRepository.findById(id)
                .orElseThrow(
                        () -> new Exception("policy not found with id")
                );
        BaggagePolicyMapper.updateEntity(request, baggagePolicy);
        BaggagePolicy saved = baggagePolicyRepository.save(baggagePolicy);
        return BaggagePolicyMapper.toResponse(saved);
    }

    @Override
    public void deleteBaggagePolicy(Long id) {
        BaggagePolicy policy = baggagePolicyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Baggage policy not found with id: " + id));
        baggagePolicyRepository.delete(policy);
    }
}
