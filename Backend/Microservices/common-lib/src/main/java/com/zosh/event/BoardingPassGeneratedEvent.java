package com.zosh.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardingPassGeneratedEvent implements Serializable {
    private String passengerName;
    private String passengerEmail;
    private String pnr;
    private String flightNumber;
    private String fromAirport;
    private String toAirport;
    private String date;
    private String boardingTime;
    private String seat;
    private String gate;
}
