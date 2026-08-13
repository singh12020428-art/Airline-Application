package com.zosh.service.impl;

import com.zosh.enums.SeatAvailabilityStatus;
import com.zosh.enums.SeatType;
import com.zosh.model.*;
import com.zosh.payload.request.FlightInstanceCabinRequest;
import com.zosh.payload.response.FlightInstanceCabinResponse;
import com.zosh.repository.*;
import com.zosh.mapper.FlightInstanceCabinMapper;
import com.zosh.service.FlightInstanceCabinService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightInstanceCabinServiceImpl implements FlightInstanceCabinService {

    private final CabinClassRepository cabinClassRepository;
    private final SeatMapRepository seatMapRepository;
    private final FlightInstanceCabinRepository flightInstanceCabinRepository;
    private final SeatInstanceRepository seatInstanceRepository;

    @Override
    public FlightInstanceCabinResponse createFlightInstanceCabin(FlightInstanceCabinRequest request) throws Exception {
        CabinClass cabinClass = cabinClassRepository.findById(request.getCabinClassId())
                .orElseThrow(() -> new Exception("Cabin class not found"));

        SeatMap seatMap = seatMapRepository.findById(cabinClass.getSeatMap().getId())
                .orElseThrow(() -> new Exception("Seat Map not found"));

        if (seatMap.getSeats() == null || seatMap.getSeats().isEmpty()) {
            throw new Exception("no seat found in seat map");
        }

        int totalSeats = seatMap.getSeats().size();

        FlightInstanceCabin cabin = FlightInstanceCabin.builder()
                .flightInstanceId(request.getFlightInstanceId())
                .cabinClass(cabinClass)
                .totalSeats(totalSeats)
                .bookedSeats(0)
                .build();

        FlightInstanceCabin savedCabin = flightInstanceCabinRepository.save(cabin);

        List<SeatInstance> seatInstances = seatMap.getSeats().stream()
                .map(
                        seat -> {
                            Double premiumSuperCharge = calculateSeatPrice(
                                    seat.getSeatRow(),
                                    seat.getSeatType()
                            );
                            SeatInstance seatInstance=SeatInstance.builder()
                                    .flightId(request.getFlightId())
                                    .status(SeatAvailabilityStatus.AVAILABLE)
                                    .flightInstanceId(request.getFlightInstanceId())
                                    .flightInstanceCabin(savedCabin)
                                    .seat(seat)
                                    .isAvailable(true)
                                    .isBooked(false)
                                    .premiumSupercharge(premiumSuperCharge)
                                    .build();
                            return seatInstance;
                        }
                ).toList();
        seatInstanceRepository.saveAll(seatInstances);
        savedCabin.setSeats(seatInstances);

        return FlightInstanceCabinMapper.toResponse(savedCabin);
    }

    @Override
    public FlightInstanceCabinResponse getFlightInstanceCabinById(Long id) {
        FlightInstanceCabin fic = flightInstanceCabinRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Flight instance cabin not found with id: " + id));
        return FlightInstanceCabinMapper.toResponse(fic);
    }

    @Override
    public Page<FlightInstanceCabinResponse> getByFlightInstanceId(Long id, Pageable pageable) {
        return flightInstanceCabinRepository.findByFlightInstanceId(id, pageable)
                .map(FlightInstanceCabinMapper::toResponse);
    }

    @Override
    public FlightInstanceCabinResponse getByFlightInstanceIdAndCabinClassId(Long flightInstanceId, Long cabinClassId) {
        FlightInstanceCabin cabin = flightInstanceCabinRepository.findFirstByFlightInstanceIdAndCabinClassId(flightInstanceId, cabinClassId);
        return FlightInstanceCabinMapper.toResponse(cabin);
    }

    @Override
    public FlightInstanceCabinResponse updateFlightInstanceCabin(Long id, FlightInstanceCabinRequest request) {
        FlightInstanceCabin fic = flightInstanceCabinRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Flight instance cabin not found with id: " + id));

        if (request.getCabinClassId() != null) {
            CabinClass cabinClass = cabinClassRepository.findById(request.getCabinClassId())
                    .orElseThrow(() -> new EntityNotFoundException("cabin class not found"));
            fic.setCabinClass(cabinClass);
        }

        FlightInstanceCabin updated = flightInstanceCabinRepository.save(fic);
        return FlightInstanceCabinMapper.toResponse(updated);
    }

    @Override
    public void deleteFlightInstanceCabin(Long id) {
        FlightInstanceCabin fic = flightInstanceCabinRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Flight instance cabin not found with id: " + id
                ));
        flightInstanceCabinRepository.delete(fic);
    }

    private Double calculateSeatPrice(Integer row, SeatType seatType) {
        if (seatType == null || row == null) return 150.0;

        // Front rows (Row 1-3)
        if (row <= 3) {
            return switch (seatType) {
                case WINDOW -> 1000.0;
                case AISLE -> 500.0;
                case MIDDLE -> 300.0;
                default -> 150.0;
            };
        }

        // Standard & Back rows (Row 4+)
        return switch (seatType) {
            case WINDOW -> 500.0;
            case AISLE -> 300.0;
            case MIDDLE -> 200.0; // Middle seats are no longer free
            default -> 150.0;
        };
    }

}