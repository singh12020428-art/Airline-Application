package com.zosh.service.integration;

import com.zosh.client.PricingClient;
import com.zosh.payload.response.FareResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FareIntegrationService {

    private final PricingClient pricingClient;

    public Double calculateFareTotal(Long fareId) {
        // Fetch fare details from PricingClient
        FareResponse fareResponse = pricingClient.getFareById(fareId);

        // Extract components safely (handle nulls)
        Double baseFare = fareResponse.getBaseFare();
        Double taxesAndFees = fareResponse.getTaxesAndFees() != null ? fareResponse.getTaxesAndFees() : 0.0;
        Double airlineFees = fareResponse.getAirlineFees() != null ? fareResponse.getAirlineFees() : 0.0;

        // Calculate total fare
        return baseFare + taxesAndFees + airlineFees;
    }
}
