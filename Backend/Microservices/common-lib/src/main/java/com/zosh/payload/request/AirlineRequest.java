package com.zosh.payload.request;

import com.zosh.embeddable.Support;
import com.zosh.enums.AirlineStatus;
import com.zosh.payload.dto.UserDTO;
import com.zosh.payload.response.CityResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirlineRequest {

    @NotBlank(message = "iata code is mandatory")
    @Size(min=2, max = 2, message = "IATA code must be exactly 2 characters")
    private String iataCode;

    @NotBlank(message = "icao code is mandatory")
    @Size(min=3, max = 3, message = "ICAO code must be exactly 3 characters")
    private String icaoCode;

    @NotBlank(message="airline name is mandatory")
    private String name;
    private String alias;

    private String logoUrl;
    private String website;
    private AirlineStatus status;
    private String alliance;
    private Long headquartersCityId;

    private String supportEmail;
    private String supportPhone;
    private String supportHours;

}
