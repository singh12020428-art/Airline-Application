package com.zosh.service;

import com.zosh.model.Booking;
import com.zosh.model.Ticket;

import java.util.List;

public interface TicketService {
    List<Ticket> generateTicketsForBooking(Booking booking);
}
