package com.zosh.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaggagePolicyResponse {
    private Long id;

    private String name;
    private String description;

    // Cabin baggage
    private Double cabinBaggageMaxWeight;
    private Integer cabinBaggagePieces;
    private Double cabinBaggageWeightPerPiece;
    private Integer cabinBaggageMaxDimension;

    // Check-in baggage
    private Double checkInBaggageMaxWeight;
    private Integer checkInBaggagePieces;
    private Double checkInBaggageWeightPerPiece;
    private Integer freeCheckedBagsAllowance;

    // Benefits
    private Boolean priorityBaggage;
    private Boolean extraBaggageAllowance;

    private Long airlineId;
    private Long fareId;

    private Instant createdAt;
    private Instant updatedAt;

}
