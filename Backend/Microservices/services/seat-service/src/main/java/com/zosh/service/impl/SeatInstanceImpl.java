package com.zosh.service.impl;

import com.zosh.enums.SeatAvailabilityStatus;
import com.zosh.mapper.SeatInstanceMapper;
import com.zosh.model.SeatInstance;
import com.zosh.payload.response.SeatInstanceResponse;
import com.zosh.repository.SeatInstanceRepository;
import com.zosh.service.SeatInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatInstanceImpl implements SeatInstanceService {

    private final SeatInstanceRepository seatInstanceRepository;
    @Override
    public Double calculateSeatPrice(List<Long> seatInstanceIds) {
        List<SeatInstance> seatInstances = seatInstanceRepository.findAllById(seatInstanceIds);

        double price=0;
        for(SeatInstance si: seatInstances){
            double seatPremium = si.getPremiumSupercharge()!=null? si.getPremiumSupercharge():0;
            price+=seatPremium;
        }
        return price;
    }

    @Override
    public List<SeatInstanceResponse> getSeatInstancesByIds(List<Long> seatInstanceIds) {
        return seatInstanceRepository.findAllById(seatInstanceIds).stream()
                .map(com.zosh.mapper.SeatInstanceMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public SeatInstanceResponse updateSeatInstanceStatus(Long seatInstanceId, SeatAvailabilityStatus status) {
        SeatInstance seatInstance = seatInstanceRepository.findById(seatInstanceId).orElse(null);
        if(seatInstance==null){return null;}
        seatInstance.setStatus(status);
        if (status == SeatAvailabilityStatus.BOOKED || status == SeatAvailabilityStatus.OCCUPIED) {
            seatInstance.setAvailable(false);
            seatInstance.setBooked(true);
        } else {
            seatInstance.setAvailable(true);
            seatInstance.setBooked(false);
        }
        seatInstanceRepository.save(seatInstance);
        return SeatInstanceMapper.toResponse(seatInstance);
    }
    @Override
    @org.springframework.transaction.annotation.Transactional
    public void lockSeats(List<Long> seatInstanceIds) throws Exception {
        List<SeatInstance> seatInstances = seatInstanceRepository.findAllById(seatInstanceIds);
        if (seatInstances.size() != seatInstanceIds.size()) {
            throw new Exception("One or more seat instances not found");
        }
        for (SeatInstance seatInstance : seatInstances) {
            if (!seatInstance.isAvailable() || seatInstance.getStatus() != SeatAvailabilityStatus.AVAILABLE) {
                throw new Exception("Seat " + (seatInstance.getSeat() != null ? seatInstance.getSeat().getSeatNumber() : seatInstance.getId()) + " is no longer available");
            }
            seatInstance.setStatus(SeatAvailabilityStatus.OCCUPIED);
            seatInstance.setAvailable(false);
            seatInstance.setBooked(false);
        }
        seatInstanceRepository.saveAll(seatInstances);
    }
}
