package com.zosh.event;

import com.zosh.client.BookingClient;
import com.zosh.enums.SeatAvailabilityStatus;
import com.zosh.payload.response.BookingResponse;
import com.zosh.payload.response.SeatInstanceResponse;
import com.zosh.service.SeatInstanceService;
import com.zosh.event.BookingExpiredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatServicePaymentEventListener {

    private final BookingClient bookingClient;
    private final SeatInstanceService seatInstanceService;

    @KafkaListener(topics = "payment-completed", groupId = "seat-service-group")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        // Fetch booking details from booking-service
        BookingResponse bookingResponse = bookingClient.getBookingById(event.getBookingId());

        if (bookingResponse == null) return;

        List<SeatInstanceResponse> seatInstances = bookingResponse.getSeatInstances();

        // Mark all seats in the booking as BOOKED
        for (SeatInstanceResponse seatInstanceResponse : seatInstances) {
            seatInstanceService.updateSeatInstanceStatus(
                    seatInstanceResponse.getId(),
                    SeatAvailabilityStatus.BOOKED
            );
        }
    }

    @KafkaListener(topics = "payment-failed", groupId = "seat-service-group")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        // Fetch booking details from booking-service
        BookingResponse bookingResponse = bookingClient.getBookingById(event.getBookingId());

        if (bookingResponse == null) return;

        List<SeatInstanceResponse> seatInstances = bookingResponse.getSeatInstances();

        // Mark all seats in the booking as AVAILABLE again
        for (SeatInstanceResponse seatInstanceResponse : seatInstances) {
            seatInstanceService.updateSeatInstanceStatus(
                    seatInstanceResponse.getId(),
                    SeatAvailabilityStatus.AVAILABLE
            );
        }
    }

    @KafkaListener(topics = "booking-expired", groupId = "seat-service-group")
    public void handleBookingExpired(BookingExpiredEvent event) {
        if (event.getSeatInstanceIds() == null) return;
        
        for (Long seatInstanceId : event.getSeatInstanceIds()) {
            seatInstanceService.updateSeatInstanceStatus(
                    seatInstanceId,
                    SeatAvailabilityStatus.AVAILABLE
            );
        }
    }
}
