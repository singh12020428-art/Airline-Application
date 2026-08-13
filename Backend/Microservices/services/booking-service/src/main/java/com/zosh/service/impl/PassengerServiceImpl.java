package com.zosh.service.impl;

import com.zosh.mapper.PassengerMapper;
import com.zosh.model.Passenger;
import com.zosh.payload.request.PassengerRequest;
import com.zosh.repository.PassengerRepository;
import com.zosh.service.PassengerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PassengerServiceImpl implements PassengerService {

    private final PassengerRepository passengerRepository;

    @Override
    public Passenger CreatePassenger(PassengerRequest passengerRequest, Long userId) {

        Passenger passenger = PassengerMapper.toEntity(passengerRequest);
        passenger.setPrimaryUserId(userId);
        Passenger saved = passengerRepository.save(passenger);
        return saved;
    }
}
