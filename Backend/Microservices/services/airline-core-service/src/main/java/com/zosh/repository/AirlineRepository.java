package com.zosh.repository;

import com.zosh.enums.AirlineStatus;
import com.zosh.model.Airline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AirlineRepository extends JpaRepository<Airline, Long> {

    Optional<Airline> findFirstByOwnerId(Long ownerId);
    List<Airline> findByStatus(AirlineStatus status);
}
