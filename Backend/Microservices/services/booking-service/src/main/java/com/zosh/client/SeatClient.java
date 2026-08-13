package com.zosh.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "seat-service")
public interface SeatClient {

    @PostMapping("/api/seat-instances/calculate/seat/price")
    Double calculateSeatPrice(@RequestBody List<Long> seatInstanceIds);

    @PostMapping("/api/seat-instances/lock")
    void lockSeats(@RequestBody List<Long> seatInstanceIds);

    @PostMapping("/api/seat-instances/by-ids")
    List<com.zosh.payload.response.SeatInstanceResponse> getSeatInstancesByIds(@RequestBody List<Long> seatInstanceIds);
}
