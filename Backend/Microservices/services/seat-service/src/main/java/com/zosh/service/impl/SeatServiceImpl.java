package com.zosh.service.impl;

import com.zosh.enums.SeatType;
import com.zosh.model.*;
import com.zosh.payload.request.SeatRequest;
import com.zosh.payload.response.SeatResponse;
import com.zosh.repository.SeatRepository;
import com.zosh.repository.SeatMapRepository;
import com.zosh.service.SeatService;
import com.zosh.mapper.SeatMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final SeatMapRepository seatMapRepository;

    @Override
    public void generateSeats(Long seatMapId) throws Exception {
        boolean exists = seatRepository.existsBySeatMapId(seatMapId);
        if (exists) {
            throw new Exception("seats already created for seat map");
        }
        SeatMap seatMap = seatMapRepository.findById(seatMapId).orElseThrow(
                () -> new Exception("seat map not found")
        );

        int leftSeatsPerRow = seatMap.getLeftSeatsPerRow();
        int rightSeatsPerRow = seatMap.getRightSeatsPerRow();
        int rows = seatMap.getTotalRows();
        int seatsPerRow = leftSeatsPerRow + rightSeatsPerRow;

        List<Seat> seats = new ArrayList<>();

        for (int row = 1; row <= rows; row++) {
            for (int col = 0; col < seatsPerRow; col++) {
                String seatNum = row + getSeatLetter(col);
                SeatType type = getSeatType(col, leftSeatsPerRow, rightSeatsPerRow);
                Seat seat = Seat.builder()
                        .seatNumber(seatNum)
                        .seatRow(row)
                        .columnLetter(getSeatLetter(col).charAt(0))
                        .seatType(type)
                        .seatMap(seatMap)
                        .build();
                seats.add(seat);
            }
        }
        seatRepository.saveAll(seats);
    }

    private SeatType getSeatType(int col, int leftSeatsPerRow, int rightSeatsPerRow) {
        int totalSeats = leftSeatsPerRow + rightSeatsPerRow;
        if (col == 0 || col == totalSeats - 1) return SeatType.WINDOW;
        if (col == leftSeatsPerRow - 1 || col == leftSeatsPerRow) return SeatType.AISLE;
        return SeatType.MIDDLE;
    }

    private String getSeatLetter(int col) {
        StringBuilder sb = new StringBuilder();
        while (col >= 0) {
            sb.insert(0, (char) ('A' + (col % 26)));
            col = col / 26 - 1;
        }
        return sb.toString();
    }

    @Override
    public List<SeatResponse> getAll() {
        return seatRepository.findAll().stream()
                .map(SeatMapper::toResponse)
                .toList();
    }

    @Override
    public SeatResponse updateSeats(Long seatId, SeatRequest request) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found with id: " + seatId));

        // Apply updates from request
        if (request.getSeatNumber() != null) seat.setSeatNumber(request.getSeatNumber());
        if (request.getSeatRow() != null) seat.setSeatRow(request.getSeatRow());
        if (request.getColumnLetter() != null) seat.setColumnLetter(request.getColumnLetter());
        if (request.getSeatType() != null) seat.setSeatType(request.getSeatType());

        if (request.getSeatMapId() != null) {
            SeatMap seatMap = seatMapRepository.findById(request.getSeatMapId())
                    .orElseThrow(() -> new RuntimeException("Seat map not found"));
            seat.setSeatMap(seatMap);
        }

        if (request.getCabinClassId() != null) {
            CabinClass cabinClass = new CabinClass();
            cabinClass.setId(request.getCabinClassId());
            seat.setCabinClass(cabinClass);
        }

        if (request.getIsAvailable() != null) seat.setIsAvailable(request.getIsAvailable());
        if (request.getIsBlocked() != null) seat.setIsBlocked(request.getIsBlocked());
        if (request.getIsEmergencyExit() != null) seat.setIsEmergencyExist(request.getIsEmergencyExit());
        if (request.getIsActive() != null) seat.setIsActive(request.getIsActive());

        if (request.getBasePrice() != null) seat.setBasePrice(request.getBasePrice());
        if (request.getPremiumSurcharge() != null) seat.setPremiumSuperCharge(request.getPremiumSurcharge());

        if (request.getHasExtraLegroom() != null) seat.setHasExtraLegroom(request.getHasExtraLegroom());
        if (request.getHasPowerOutlet() != null) seat.setHasPowerOutlet(request.getHasPowerOutlet());
        if (request.getHasTvScreen() != null) seat.setHasTvScreen(request.getHasTvScreen());
        if (request.getHasExtraWidth() != null) seat.setHasExtraWidth(request.getHasExtraWidth());

        if (request.getSeatPitch() != null) seat.setSeatPitch(request.getSeatPitch());
        if (request.getSeatWidth() != null) seat.setSeatWidth(request.getSeatWidth());

        // Save and return response
        Seat updatedSeat = seatRepository.save(seat);
        return SeatMapper.toResponse(updatedSeat);
    }

}
