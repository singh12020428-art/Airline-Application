package com.zosh.job;

import com.zosh.enums.BookingStatus;
import com.zosh.event.publisher.BookingEventProducer;
import com.zosh.model.Booking;
import com.zosh.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingExpiryJob {

    private final BookingRepository bookingRepository;
    private final BookingEventProducer bookingEventProducer;

    // Run every minute
    @Scheduled(fixedRate = 60000)
    @org.springframework.transaction.annotation.Transactional
    public void expirePendingBookings() {
        log.info("Running Booking Expiry Job...");

        Instant fiveMinutesAgo = Instant.now().minus(5, ChronoUnit.MINUTES);

        List<Booking> expiredBookings = bookingRepository.findByStatusAndBookingDateBefore(
                BookingStatus.PENDING, fiveMinutesAgo);

        for (Booking booking : expiredBookings) {
            log.info("Expiring booking: {}", booking.getId());
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            // Notify seat-service to release the seats
            bookingEventProducer.sendBookingExpired(booking);
        }
    }
}
