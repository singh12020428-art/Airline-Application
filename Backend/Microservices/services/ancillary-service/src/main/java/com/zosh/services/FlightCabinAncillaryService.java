package com.zosh.services;

import com.zosh.enums.AncillaryType;
import com.zosh.model.FlightCabinAncillary;
import com.zosh.payload.request.FlightCabinAncillaryRequest;
import com.zosh.payload.response.FlightCabinAncillaryResponse;

import java.util.List;

public interface FlightCabinAncillaryService {

    FlightCabinAncillaryResponse create(FlightCabinAncillaryRequest request) throws Exception;

    FlightCabinAncillaryResponse getById(Long id) throws Exception;

    List<FlightCabinAncillaryResponse> getByFlightAndCabinClass(
            Long flightId,
            Long cabinClassId
    );

    List<FlightCabinAncillaryResponse> getByFlightId(Long flightId);

    List<FlightCabinAncillaryResponse> getAllByIds(List<Long> ids);

    FlightCabinAncillaryResponse getByFlightIdAndCabinClassIdAndType(
            Long flightId,
            Long cabinClassId,
            AncillaryType type
    );

    List<FlightCabinAncillaryResponse> getAllByFlightIdAndCabinClassIdAndType(
            Long flightId,
            Long cabinClassId,
            AncillaryType type
    );

    FlightCabinAncillaryResponse update(
            Long id,
            FlightCabinAncillaryRequest request
    ) throws Exception;

    void delete(Long id) throws Exception;

    Double calculateAncillaryPrice(List<Long> ancillaryIds);
}