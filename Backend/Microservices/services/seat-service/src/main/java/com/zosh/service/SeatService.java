package com.zosh.service;

import com.zosh.payload.request.SeatRequest;
import com.zosh.payload.response.SeatResponse;

import java.util.List;

public interface SeatService {
    void generateSeats(Long seatMapId) throws Exception;
    List<SeatResponse> getAll();
    SeatResponse updateSeats(Long seatId, SeatRequest request);
}
