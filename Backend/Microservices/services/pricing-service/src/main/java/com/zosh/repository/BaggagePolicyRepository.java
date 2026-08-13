package com.zosh.repository;

import com.zosh.model.BaggagePolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BaggagePolicyRepository extends JpaRepository<BaggagePolicy, Long> {

    BaggagePolicy findByFareId(Long fareId);
    List<BaggagePolicy> findByAirlineId(Long airlineId);
    boolean existsByFareId(Long fareId);
}
