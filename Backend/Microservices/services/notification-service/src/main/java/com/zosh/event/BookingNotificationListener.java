package com.zosh.event;

import com.zosh.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
@RequiredArgsConstructor
public class BookingNotificationListener {

    private final EmailService emailService;

    @KafkaListener(
            topics = "booking.confirmed",
            groupId = "notification-service-group"
    )
    public void handleBookingConfirmed(BookingConfirmedEvent event) throws MessagingException, UnsupportedEncodingException {
        System.out.println("========== BOOKING EVENT RECEIVED ==========");
        System.out.println(event);

        emailService.sendBookingConfirmation(event);
    }

}
