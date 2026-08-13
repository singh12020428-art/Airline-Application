package com.zosh.flight_ops_service.event;

import com.zosh.event.FlightInstanceCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FlightInstanceEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;


    public void sendFlightInstanceCreated(FlightInstanceCreatedEvent event) {
        kafkaTemplate.send("flight-instance-created", event);
    }
}