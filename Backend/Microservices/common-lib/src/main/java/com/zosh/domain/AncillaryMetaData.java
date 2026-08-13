package com.zosh.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AncillaryMetaData {

    private BaggageMetaData baggage;
    private String protectionSummary;
    private String specialServiceDetails;
    private String upgradeDetails;
}
