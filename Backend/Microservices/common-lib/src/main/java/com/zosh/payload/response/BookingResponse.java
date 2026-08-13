package com.zosh.payload.response;

import com.zosh.embeddable.ContactInfo;
import com.zosh.enums.BookingStatus;
import com.zosh.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BookingResponse {

    private Long id;
    private String bookingReference;

    private Long userId;
    private String userName;
    private String userEmail;

    private Long flightId;
    private String flightNumber;
    private String flightName;
    private String departureAirport;
    private String arrivalAirport;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    private BookingStatus status;
    private Instant bookingDate;
    private Instant lastModified;

    private List<PassengerResponse> passengers;
    private List<SeatInstanceResponse> seatInstances;
    private PaymentLinkResponse payment;
    private List<FlightCabinAncillaryResponse> ancillaries;
    private List<FlightMealResponse> meals;
    private List<TicketResponse> tickets;

    //payment details
    private PaymentStatus paymentStatus;
    private String paymentLink;

    // Derived info
    private Integer totalPassengers;
    private Double totalAmount;
    private String flightDuration;
    private Boolean isUpcoming;
    private Boolean isPast;

    private Long fareId;
    private String fareName;
    private Double fareBaseFare;
    private Double fareTaxesAndFees;
    private Double fareAirlineFees;



    private ContactInfo contactInfo;
}
