package com.zosh.event;

import com.zosh.enums.SeatAvailabilityStatus;
import com.zosh.enums.SeatType;
import com.zosh.model.CabinClass;
import com.zosh.model.FlightInstanceCabin;
import com.zosh.model.Seat;
import com.zosh.model.SeatInstance;
import com.zosh.repository.CabinClassRepository;
import com.zosh.repository.FlightInstanceCabinRepository;
import com.zosh.repository.SeatInstanceRepository;
import com.zosh.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightInstanceEventConsumer {

    private final CabinClassRepository cabinClassRepository;
    private final SeatRepository seatRepository;
    private final FlightInstanceCabinRepository flightInstanceCabinRepository;
    private final SeatInstanceRepository seatInstanceRepository;

    @KafkaListener(topics = "flight-instance-created", groupId = "seat-service-group")
    @Transactional
    public void handleFlightInstanceCreated(FlightInstanceCreatedEvent event) {

        // Fetch cabin classes for the aircraft
        List<CabinClass> cabinClasses = cabinClassRepository.findByAircraftId(event.getAircraftId());

        int totalSeatInstances = 0;

        for (CabinClass cabinClass : cabinClasses) {
            List<Seat> seats = cabinClass.getSeatMap() != null
                    ? seatRepository.findBySeatMapId(cabinClass.getSeatMap().getId())
                    : List.of();

            // Create FlightInstanceCabin entry
            FlightInstanceCabin fic = FlightInstanceCabin.builder()
                    .flightInstanceId(event.getFlightInstanceId())
                    .cabinClass(cabinClass)
                    .totalSeats(seats.size())
                    .bookedSeats(0)
                    .build();

            FlightInstanceCabin savedFic = flightInstanceCabinRepository.save(fic);

            // Create SeatInstances for this cabin
            List<SeatInstance> seatInstances = seats.stream().map(
                    seat -> SeatInstance.builder()
                            .flightId(event.getFlightId())
                            .flightInstanceId(event.getFlightInstanceId())
                            .flightInstanceCabin(savedFic)
                            .seat(seat)
                            .status(SeatAvailabilityStatus.AVAILABLE)
                            .isBooked(false)
                            .isAvailable(true)
                            .premiumSupercharge(getPremiumSuperCharge(
                                    seat.getSeatType(),
                                    1000.0, // window surcharge
                                    500.0   // aisle surcharge
                            ))
                            .build()
            ).toList();

            seatInstanceRepository.saveAll(seatInstances);
            totalSeatInstances += seatInstances.size();
        }

        // Optionally log or publish a summary event
        System.out.println("FlightInstanceCreated: " + event.getFlightInstanceId() +
                " with total seats = " + totalSeatInstances);
    }

    private Double getPremiumSuperCharge(SeatType seatType,
                                         Double windowSuperCharge,
                                         Double aisleSuperCharge) {
        if (seatType == null) return 0.0;

        return switch (seatType) {
            case AISLE -> aisleSuperCharge;
            case WINDOW -> windowSuperCharge;
            default -> 0.0;
        };
    }
}
