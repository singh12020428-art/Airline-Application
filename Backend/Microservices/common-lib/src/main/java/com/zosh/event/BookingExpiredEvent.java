package com.zosh.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingExpiredEvent {

    private Long bookingId;
    private Long userId;
    private List<Long> seatInstanceIds;
    private LocalDateTime expiredAt;

}
