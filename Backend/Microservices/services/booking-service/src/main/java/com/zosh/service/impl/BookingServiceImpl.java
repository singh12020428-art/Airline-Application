package com.zosh.service.impl;

import com.zosh.client.*;
import com.zosh.enums.BookingStatus;
import com.zosh.enums.PaymentGateway;
import com.zosh.enums.PaymentStatus;
import com.zosh.mapper.BookingMapper;
import com.zosh.model.Booking;
import com.zosh.model.Passenger;
import com.zosh.payload.request.BookingRequest;
import com.zosh.payload.request.PassengerRequest;
import com.zosh.payload.request.PaymentInitiateRequest;
import com.zosh.payload.response.*;
import com.zosh.payload.dto.PaymentDTO;
import com.zosh.repository.BookingRepository;
import com.zosh.service.BookingService;
import com.zosh.service.PassengerService;
import com.zosh.service.TicketService;
import com.zosh.service.integration.FareIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final com.zosh.repository.TicketRepository ticketRepository;
    private final PassengerService passengerService;
    private final TicketService ticketService;
    private final FlightClient flightClient;
    private final FareIntegrationService fareIntegrationService;
    private final SeatClient seatClient;
    private final PricingClient pricingClient;
    private final AncillaryClient ancillaryClient;
    private final PaymentClient paymentClient;
    private final AirlineClient airlineClient;
    private final org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public PaymentInitiateResponse createBooking(BookingRequest request, Long userId) {
        // step 1: create unique booking reference
        String bookingReference = generateBookingReference();

        // step 2: create passengers
        Set<Passenger> passengers = new HashSet<>();
        for (PassengerRequest passengerRequest : request.getPassengers()) {
            Passenger passenger = passengerService.CreatePassenger(passengerRequest, userId);
            passengers.add(passenger);
        }

        // step 3: flight existence check
        FlightResponse flightResponse = flightClient.getFlightById(request.getFlightId());

        // step 4: create booking entity
        Booking booking = BookingMapper.toEntity(request, userId, passengers, bookingReference);
        booking.setStatus(BookingStatus.PENDING);

        // step 5: set airline id  fetch from flight service
        booking.setAirlineId(flightResponse.getAirline().getId());

        // step 6: set seat instance ids
        List<Long> seatInstanceIds = request.getPassengers().stream()
                .map(PassengerRequest::getSeatInstanceId)
                .collect(Collectors.toList());
        booking.setSeatInstanceIds(seatInstanceIds);

        // step 6.5: lock seats synchronously
        try {
            if (!seatInstanceIds.isEmpty()) {
                seatClient.lockSeats(seatInstanceIds);
            }
        } catch (Exception e) {
            throw new RuntimeException("One or more selected seats are no longer available. Please select different seats.");
        }

        booking = bookingRepository.save(booking);

        // step 7: set booking reference on passengers
        for (Passenger passenger : passengers) {
            passenger.setBooking(booking);
        }

        // step 8: generate tickets
        ticketService.generateTicketsForBooking(booking);

        // step 9: calculate price
            //    1. calculate fare total
        Double fareTotal = fareIntegrationService.calculateFareTotal(request.getFareId());
             //   2. seat price
        Double seatPrice = seatClient.calculateSeatPrice(booking.getSeatInstanceIds());
             //   3. ancillary price
        Double ancillaryPrice = ancillaryClient.calculateAncillariesPrice(booking.getAncillaryIds());
             //   4. meal price
        Double mealPrice = ancillaryClient.calculateMealPrice(booking.getMealsIds());
        
        Double totalFareForAllPassengers = fareTotal * passengers.size();
        Double totalPrice = totalFareForAllPassengers + mealPrice + seatPrice + ancillaryPrice;

        // step 10: initiate payment
        PaymentInitiateRequest paymentInitiateRequest = PaymentInitiateRequest.builder()
                .userId(userId)
                .bookingId(booking.getId())
                .amount(totalPrice)
                .gateway(PaymentGateway.RAZORPAY)
                .description("Payment for booking : " + bookingReference)
                .build();

        return paymentClient.initiatePayment(paymentInitiateRequest);
    }

    @Override
    public BookingResponse updateBooking(Long id, BookingRequest request) {
        // TODO: implement update logic
        return null;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public BookingResponse getBookingById(Long id) throws Exception {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new Exception("booking not found with id"));

        return convertToBookingResponse(booking);
    }

    @Override
    public List<BookingResponse> getAllBookingsByAirline(Long userId,
                                                         String roles,
                                                         String searchQuery,
                                                         BookingStatus status,
                                                         Long flightInstanceId,
                                                         String sortDirection) {

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Sort sort = Sort.by(direction, "bookingDate");
        List<Booking> bookings;

        if (roles != null && roles.contains("ROLE_SYSTEM_ADMIN")) {
            bookings = bookingRepository.findAllWithFilter(
                    searchQuery,
                    status,
                    flightInstanceId,
                    sort
            );
        } else {
            AirlineResponse airlineResponse = airlineClient.getAirlineByOwner(userId);
            bookings = bookingRepository.findByAirlineWithFilter(
                            airlineResponse.getId(),
                            searchQuery,
                            status,
                            flightInstanceId,
                            sort
                    );
        }

        return bookings.stream()
                .map(this::convertToBookingResponse)
                .toList();
    }

    @Override
    public List<BookingResponse> getBookingsByUser(Long userId) {

        return bookingRepository.findByUserId(userId)
                .stream()
                .map(this::convertToBookingResponse)
                .toList();
    }

    @Override
    public BookingResponse cancelBooking(Long id) throws Exception {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new Exception("booking not found with id"));

        booking.setStatus(BookingStatus.CANCELLED);

        Booking updated = bookingRepository.save(booking);

        return convertToBookingResponse(updated);
    }

    @Override
    public void deleteBooking(Long id) throws Exception {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new Exception("booking not found with id"));

        bookingRepository.delete(booking);
    }

    private String generateBookingReference() {
        String reference;
        do {
            reference = "BK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (bookingRepository.existsByBookingReference(reference));
        return reference;
    }

    private BookingResponse convertToBookingResponse(Booking booking) {
        List<FlightCabinAncillaryResponse> ancillaryResponses = new ArrayList<>();
        try {
            if (booking.getAncillaryIds() != null && !booking.getAncillaryIds().isEmpty()) {
                for (Long id : booking.getAncillaryIds()) {
                    FlightCabinAncillaryResponse res = ancillaryClient.getAncillaryById(id);
                    if (res != null) ancillaryResponses.add(res);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching ancillaries for booking " + booking.getId());
        }

        List<FlightMealResponse> mealResponses = new ArrayList<>();
        try {
            if (booking.getMealsIds() != null && !booking.getMealsIds().isEmpty()) {
                List<FlightMealResponse> res = ancillaryClient.getMealsByIds(booking.getMealsIds());
                if (res != null) mealResponses.addAll(res);
            }
        } catch (Exception e) {
            System.err.println("Error fetching meals for booking " + booking.getId());
        }

        PaymentDTO paymentDTO = new PaymentDTO();
        
        FareResponse fareResponse = null;
        try {
            if (booking.getFareId() != null) {
                fareResponse = pricingClient.getFareById(booking.getFareId());
            }
        } catch (Exception e) {
            System.err.println("Error fetching fare for booking " + booking.getId());
        }

        FlightResponse flightResponse = null;
        try {
            if (booking.getFlightId() != null) {
                flightResponse = flightClient.getFlightById(booking.getFlightId());
            }
        } catch (Exception e) {
            System.err.println("Error fetching flight for booking " + booking.getId());
        }

        FlightInstanceResponse flightInstanceResponse = null;
        try {
            if (booking.getFlightInstanceId() != null) {
                flightInstanceResponse = flightClient.getFlightInstanceById(booking.getFlightInstanceId());
            }
        } catch (Exception e) {
            System.err.println("Error fetching flight instance for booking " + booking.getId());
        }

        List<SeatInstanceResponse> seatInstanceResponses = new ArrayList<>();
        try {
            if (booking.getSeatInstanceIds() != null && !booking.getSeatInstanceIds().isEmpty()) {
                List<SeatInstanceResponse> res = seatClient.getSeatInstancesByIds(booking.getSeatInstanceIds());
                if (res != null) seatInstanceResponses.addAll(res);
            }
        } catch (Exception e) {
            System.err.println("Error fetching seats for booking " + booking.getId());
        }

        return BookingMapper.toResponse(
                booking,
                paymentDTO,
                fareResponse,
                flightResponse,
                flightInstanceResponse,
                ancillaryResponses,
                mealResponses,
                seatInstanceResponses
        );
    }
}
