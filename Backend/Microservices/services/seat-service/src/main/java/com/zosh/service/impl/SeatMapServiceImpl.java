package com.zosh.service.impl;

import com.zosh.client.AirlineClient;
import com.zosh.mapper.SeatMapMapper;
import com.zosh.model.CabinClass;
import com.zosh.model.SeatMap;
import com.zosh.payload.request.SeatMapRequest;
import com.zosh.payload.response.AirlineResponse;
import com.zosh.payload.response.SeatMapResponse;
import com.zosh.repository.CabinClassRepository;
import com.zosh.repository.SeatMapRepository;
import com.zosh.repository.SeatRepository;
import com.zosh.service.SeatMapService;
import com.zosh.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatMapServiceImpl implements SeatMapService {

    private final SeatMapRepository seatMapRepository;
    private final CabinClassRepository cabinClassRepository;
    private final SeatService seatService;
    private final AirlineClient airlineClient;
    private final SeatRepository seatRepository;

    @Override
    public SeatMapResponse createSeatMap(Long userId, SeatMapRequest request) throws Exception {
        AirlineResponse airlineResponse = airlineClient.getAirlineByOwner(userId);
        CabinClass cabinClass = cabinClassRepository.findById(request.getCabinClassId())
                .orElseThrow(
                        () -> new Exception("cabin class not found with given id")
                );

        if (seatMapRepository.existsByAirlineIdAndCabinClassIdAndName(
                airlineResponse.getId(), request.getCabinClassId(), request.getName()
        )) {
            throw new Exception("cabin class already exists with given name");
        }

        SeatMap seatMap = SeatMapMapper.toEntity(request, cabinClass);
        seatMap.setAirlineId(airlineResponse.getId());
        SeatMap savedSeatMap = seatMapRepository.save(seatMap);

        seatService.generateSeats(savedSeatMap.getId());

        // Reload from database and explicitly fetch seats
        SeatMap updatedSeatMap = seatMapRepository.findById(savedSeatMap.getId())
                .orElse(savedSeatMap);
        updatedSeatMap.setSeats(seatRepository.findBySeatMapId(savedSeatMap.getId()));

        return SeatMapMapper.toResponse(updatedSeatMap);
    }

    @Override
    public SeatMapResponse getSeatMapById(Long id) throws Exception {
        SeatMap seatMap = seatMapRepository.findById(id).orElseThrow(
                () -> new Exception("seat map not found with id")
        );
        if (seatMap.getSeats() == null || seatMap.getSeats().isEmpty()) {
            seatMap.setSeats(seatRepository.findBySeatMapId(seatMap.getId()));
        }
        return SeatMapMapper.toResponse(seatMap);
    }

    @Override
    public SeatMapResponse getSeatMapByCabinClass(Long cabinId) {
        SeatMap seatMap = seatMapRepository.findByCabinClassId(cabinId);
        if (seatMap != null && (seatMap.getSeats() == null || seatMap.getSeats().isEmpty())) {
            seatMap.setSeats(seatRepository.findBySeatMapId(seatMap.getId()));
        }
        return SeatMapMapper.toResponse(seatMap);
    }

    @Override
    public SeatMapResponse updateSeatMap(Long id, SeatMapRequest request) throws Exception {
        SeatMap seatMap = seatMapRepository.findById(id).orElseThrow(
                () -> new Exception("seat map not found with id")
        );
        SeatMapMapper.updateEntity(request, seatMap);
        SeatMap updated = seatMapRepository.save(seatMap);
        return SeatMapMapper.toResponse(updated);
    }

    @Override
    public void deleteSeatMap(Long id) throws Exception {
        SeatMap seatMap = seatMapRepository.findById(id).orElseThrow(
                () -> new Exception("seat map not found with id")
        );
        seatMapRepository.delete(seatMap);
    }
}
