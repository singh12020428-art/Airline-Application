package com.zosh.payload.response;

import com.zosh.enums.TicketStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketResponse {

    private Long id;

    private String ticketNumber;
    private TicketStatus status;
    private LocalDateTime issuedAt;

    // Booking details
    private Long bookingId;
    private String bookingReference;

    // Passenger details
    private Long passengerId;
    private String passengerFirstName;
    private String passengerLastName;
    private String passengerEmail;

    // Payment details
    private Long paymentId;
    private Double paymentAmount;

}
