package com.zosh.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BoardingPassNotificationListener {

    @KafkaListener(topics = "boarding-pass-events", groupId = "notification-group")
    public void handleBoardingPassGeneratedEvent(com.zosh.event.BoardingPassGeneratedEvent event) {
        log.info("Received Boarding Pass Generated Event for PNR: {}", event.getPnr());
        
        // Mock email sending logic for Boarding Pass PDF
        sendBoardingPassEmail(event);
    }

    private void sendBoardingPassEmail(com.zosh.event.BoardingPassGeneratedEvent event) {
        log.info("=====================================================");
        log.info("SENDING EMAIL TO: {}", event.getPassengerEmail());
        log.info("SUBJECT: Your Boarding Pass for Flight {}", event.getFlightNumber());
        log.info("BODY:");
        log.info("Dear {},", event.getPassengerName());
        log.info("You are checked in! Attached is your boarding pass.");
        log.info("Flight: {} | Date: {}", event.getFlightNumber(), event.getDate());
        log.info("From: {} | To: {}", event.getFromAirport(), event.getToAirport());
        log.info("Gate: {} | Seat: {} | Boarding Time: {}", event.getGate(), event.getSeat(), event.getBoardingTime());
        log.info("Safe travels!");
        log.info("=====================================================");
    }
}
