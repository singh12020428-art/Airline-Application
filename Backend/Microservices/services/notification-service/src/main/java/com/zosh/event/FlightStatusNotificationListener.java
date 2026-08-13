package com.zosh.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.zosh.enums.FlightStatus;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlightStatusNotificationListener {

    @KafkaListener(topics = "flight-status-events", groupId = "notification-group")
    public void handleFlightStatusEvent(FlightStatusUpdatedEvent event) {
        log.info("Notification Service received Flight Status Update: Flight {} is now {}", 
                 event.getFlightNumber(), event.getStatus());
        
        // Here you would typically look up all passengers for this flight
        // and send them an email or SMS notification about the status change.
        if (event.getStatus() == FlightStatus.DELAYED) {
            log.info("Sending DELAY notification emails to passengers of flight {}...", event.getFlightNumber());
        } else if (event.getStatus() == FlightStatus.CANCELLED) {
            log.info("Sending CANCELLATION notification emails to passengers of flight {}...", event.getFlightNumber());
        }
    }
}
