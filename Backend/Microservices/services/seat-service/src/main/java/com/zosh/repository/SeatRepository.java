package com.zosh.repository;

import com.zosh.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    boolean existsBySeatMapId(Long seatMapId);
    List<Seat> findBySeatMapId(Long id);
}
