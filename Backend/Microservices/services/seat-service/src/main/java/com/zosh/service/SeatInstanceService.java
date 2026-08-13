package com.zosh.service;

import com.zosh.enums.SeatAvailabilityStatus;
import com.zosh.payload.response.SeatInstanceResponse;

import java.util.List;

public interface SeatInstanceService {

    Double calculateSeatPrice(List<Long> seatInstanceIds);

    List<SeatInstanceResponse> getSeatInstancesByIds(List<Long> seatInstanceIds);
    SeatInstanceResponse updateSeatInstanceStatus(Long seatInstanceId, SeatAvailabilityStatus status);
    void lockSeats(List<Long> seatInstanceIds) throws Exception;
}
