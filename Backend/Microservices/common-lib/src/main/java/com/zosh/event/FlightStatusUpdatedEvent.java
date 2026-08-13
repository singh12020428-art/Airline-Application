package com.zosh.event;

import com.zosh.enums.FlightStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightStatusUpdatedEvent implements Serializable {
    private String flightNumber;
    private String airline;
    private String fromCode;
    private String fromCity;
    private String toCode;
    private String toCity;
    private LocalDateTime scheduledDeparture;
    private LocalDateTime estimatedDeparture;
    private LocalDateTime scheduledArrival;
    private LocalDateTime estimatedArrival;
    private FlightStatus status;
    private String terminal;
    private String gate;
    private String belt;
    private String delayDuration;
}
