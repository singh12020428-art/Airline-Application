package com.zosh.flight_ops_service.event;

import com.zosh.event.FlightStatusUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlightStatusKafkaListener {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "flight-status-events", groupId = "flight-ops-group")
    public void handleFlightStatusUpdate(FlightStatusUpdatedEvent event) {
        log.info("Received Flight Status Update for {}: {}", event.getFlightNumber(), event.getStatus());
        
        // Broadcast to WebSocket topic
        messagingTemplate.convertAndSend("/topic/flights/" + event.getFlightNumber(), event);
        messagingTemplate.convertAndSend("/topic/flights/all", event);
    }
}
