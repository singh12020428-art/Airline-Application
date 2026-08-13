package com.zosh.mapper;

import com.zosh.enums.SeatAvailabilityStatus;
import com.zosh.model.SeatInstance;
import com.zosh.payload.response.SeatInstanceResponse;

public class SeatInstanceMapper {

    public static SeatInstanceResponse toResponse(SeatInstance si) {
        if (si == null) return null;

        return SeatInstanceResponse.builder()
                .id(si.getId())
                .flightId(si.getFlightId())
                .seatId(si.getSeat() != null ? si.getSeat().getId() : null)
                .seatNumber(si.getSeat() != null ? si.getSeat().getSeatNumber() : null)
                .seatType(si.getSeat() != null ? si.getSeat().getSeatType().name() : null)
                .seatPosition(si.getSeat() != null ? si.getSeat().getFullPosition() : null)
                .seat(SeatMapper.toResponse(si.getSeat()))
                .status(si.getStatus())
                .flightInstanceId(si.getFlightInstanceId())
                .flightCabinId(si.getFlightInstanceCabin() != null ? si.getFlightInstanceCabin().getId() : null)
                .fare(si.getFare())
                .price(calculateSeatPrice(si.getSeat() != null ? si.getSeat().getSeatRow() : null, si.getSeat() != null ? si.getSeat().getSeatType() : null))
                .version(si.getVersion())
                .createdAt(si.getCreatedAt())
                .updatedAt(si.getUpdatedAt())
                .isAvailable(si.isAvailable())
                .isBooked(si.isBooked())
                .isOccupied(si.getStatus() == SeatAvailabilityStatus.OCCUPIED)
                .build();
    }

    private static Double calculateSeatPrice(Integer row, com.zosh.enums.SeatType seatType) {
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
