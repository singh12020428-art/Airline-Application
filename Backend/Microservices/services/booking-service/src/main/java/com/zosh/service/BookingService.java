package com.zosh.service;

import com.zosh.enums.BookingStatus;
import com.zosh.payload.request.BookingRequest;
import com.zosh.payload.response.BookingResponse;
import com.zosh.payload.response.PaymentInitiateResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BookingService {

    PaymentInitiateResponse createBooking(BookingRequest request, Long userId);

    BookingResponse updateBooking(Long id, BookingRequest request);

    BookingResponse getBookingById(Long id) throws Exception;

    List<BookingResponse> getAllBookingsByAirline(
            Long userId,
            String roles,
            String searchQuery,
            BookingStatus status,
            Long flightInstanceId,
            String sortDirection
    );

    List<BookingResponse> getBookingsByUser(Long userId);

    BookingResponse cancelBooking(Long id) throws Exception;

    void deleteBooking(Long id) throws Exception;

}
