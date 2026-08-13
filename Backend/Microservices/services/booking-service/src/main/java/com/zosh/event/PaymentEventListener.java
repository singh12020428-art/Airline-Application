package com.zosh.event;

import com.zosh.client.FlightClient;
import com.zosh.client.PricingClient;
import com.zosh.client.UserClient;
import com.zosh.enums.BookingStatus;
import com.zosh.event.publisher.BookingEventProducer;
import com.zosh.model.Booking;
import com.zosh.payload.dto.UserDTO;
import com.zosh.payload.response.FareResponse;
import com.zosh.payload.response.FlightInstanceResponse;
import com.zosh.repository.BookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentEventListener {

    private final BookingRepository bookingRepository;
    private final BookingEventProducer bookingEventProducer;
    private final FlightClient flightClient;
    private final PricingClient pricingClient;
    private final UserClient userClient;


    @KafkaListener(topics = "payment-completed", groupId = "booking-service-group")
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) throws Exception {

        System.out.println("Recived PaymentCompletedEvent " +event.getBookingId() + "-" + event.getPaymentId());

        Booking booking = bookingRepository.findById(event.getBookingId())
                .orElse(null);

        if (booking == null) return;

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // Example downstream calls
        FlightInstanceResponse flightInstanceResponse = flightClient.getFlightInstanceById(booking.getFlightInstanceId());
       FareResponse fareResponse = pricingClient.getFareById(booking.getFareId());
        UserDTO userDTO = null;
        try {
            userDTO = userClient.getUserById(booking.getUserId());
        } catch (Exception e) {
            System.err.println("Failed to fetch user details for booking event: " + e.getMessage());
        }

        bookingEventProducer.sendBookingConfirmed(booking, event , flightInstanceResponse , fareResponse, userDTO);

    }

    @KafkaListener(topics = "payment-failed", groupId = "booking-service-group")
    public void handlePaymentFailed(PaymentFailedEvent event) throws Exception{
        Booking booking = bookingRepository.findById(event.getBookingId())
                .orElse(null);

        if (booking == null) return;

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);


    }
}
