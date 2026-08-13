package com.zosh.controller;

import com.zosh.service.SeatInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import com.zosh.payload.response.SeatInstanceResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seat-instances")
public class SeatInstanceController {

    private final SeatInstanceService seatInstanceService;

    @PostMapping("/calculate/seat/price")
    public Double calculateSeatPrice(@RequestBody List<Long> seatInstanceIds){
        return seatInstanceService.calculateSeatPrice(seatInstanceIds);
    }

    @PostMapping("/by-ids")
    public List<SeatInstanceResponse> getSeatInstancesByIds(@RequestBody List<Long> seatInstanceIds){
        return seatInstanceService.getSeatInstancesByIds(seatInstanceIds);
    }

    @PostMapping("/lock")
    public ResponseEntity<String> lockSeats(@RequestBody List<Long> seatInstanceIds) {
        try {
            seatInstanceService.lockSeats(seatInstanceIds);
            return ResponseEntity.ok("Seats locked successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
