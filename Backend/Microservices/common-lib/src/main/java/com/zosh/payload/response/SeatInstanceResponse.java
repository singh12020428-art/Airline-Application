package com.zosh.payload.response;

import com.zosh.enums.CabinClassType;
import com.zosh.enums.SeatAvailabilityStatus;
import lombok.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatInstanceResponse {

    private Long id;

    private Long flightId;
    private Long seatId;
    private String seatNumber;
    private String seatType;
    private String seatPosition;

    private SeatResponse seat;

    private Double price;
    private Double fare;

    private SeatAvailabilityStatus status;

    private Long flightInstanceId;

    private Boolean isBooked;
    private Boolean isAvailable;
    private Boolean isOccupied;

    private Long flightCabinId;
    private CabinClassType flightCabinClassType;

    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    private String seatCharacteristics;
}
