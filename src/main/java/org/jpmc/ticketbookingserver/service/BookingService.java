package org.jpmc.ticketbookingserver.service;

import jakarta.transaction.Transactional;
import org.jpmc.ticketbookingserver.api.request.CancelTicketRQ;
import org.jpmc.ticketbookingserver.api.request.EventRQ;
import org.jpmc.ticketbookingserver.api.request.TicketRQ;
import org.jpmc.ticketbookingserver.api.response.*;
import org.jpmc.ticketbookingserver.entity.Event;
import org.jpmc.ticketbookingserver.entity.Seat;
import org.jpmc.ticketbookingserver.entity.Ticket;
import org.jpmc.ticketbookingserver.exception.ResourceDuplicateException;
import org.jpmc.ticketbookingserver.exception.ResourceNotFoundException;
import org.jpmc.ticketbookingserver.exception.UnauthorizedMethodException;
import org.jpmc.ticketbookingserver.repository.EventRepository;
import org.jpmc.ticketbookingserver.repository.SeatRepository;
import org.jpmc.ticketbookingserver.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.jpmc.ticketbookingserver.constants.ErrorMessageConstant.*;

@Service("bookingService")
@Transactional
public class BookingService {

    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final String ROWS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Autowired
    BookingService(SeatRepository seatRepository, EventRepository eventRepository, TicketRepository ticketRepository) {
        this.seatRepository = seatRepository;
        this.eventRepository = eventRepository;
        this.ticketRepository = ticketRepository;
    }

    public Event createEvent(EventRQ eventRQ) throws ResourceDuplicateException {
        if (eventRepository.existsByEventNumber(eventRQ.getEventNumber())) {
            throw new ResourceDuplicateException(EVENT_ALREADY_EXISTS);
        }
        Event event = new Event(eventRQ);
        List<Seat> seats = new ArrayList<Seat>();
        for (Integer row = 0; row < eventRQ.getRowCount(); row++) {
            String currentRow = ROWS.substring(row, row + 1);
            for (Integer column = 1; column <= eventRQ.getSeatsPerRow(); column++) {
                seats.add(new Seat(event, currentRow + Integer.toString(column)));
            }
        }
        event.setSeats(seats);
        Event result = eventRepository.save(event);
        return result;
    }

    public List<String> getAvailableSeats(Long eventNumber) throws ResourceNotFoundException {
        Event event = eventRepository.findByEventNumber(eventNumber)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND));
        List<Seat> seats = seatRepository.findAllByEventAndTicketIsNull(event);
        return seats.stream().map(seat -> seat.getSeatNumber()).collect(Collectors.toList());
    }

    public List<TicketBookingRS> bookSeats(TicketRQ request, Long eventNumber) throws ResourceNotFoundException, ResourceDuplicateException {
        Event event = eventRepository.findByEventNumber(eventNumber)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND));
        List<Ticket> tickets = new ArrayList<Ticket>();
        boolean exists = ticketRepository.existsByEventAndPhoneNumber(event, request.getPhoneNumber());
        if (exists) {
            throw new ResourceDuplicateException(TICKET_ALREADY_BOOKED_BY_ANOTHER_BUYER);
        }
        List<Seat> eventSeats = seatRepository.findAllByEventAndSeatNumberIn(event, request.getSeats());
        List<Seat> bookedSeats = eventSeats.stream().filter(seat -> seat.getTicket() != null).collect(Collectors.toList());
        List<Seat> vacantSeats = eventSeats.stream().filter(seat -> seat.getTicket() == null).collect(Collectors.toList());
        vacantSeats.forEach(seat -> {
            Ticket ticket = new Ticket();
            ticket.setSeat(seat);
            ticket.setEvent(event);
            ticket.setUpdatedAt(new Timestamp(Instant.now().toEpochMilli()));
            ticket.setTicketNumber(Long.toString(event.getEventNumber()) + seat.getSeatNumber());
            ticket.setPhoneNumber(request.getPhoneNumber());
            seat.setTicket(ticket);
        });
        List<TicketBookingRS> results = new ArrayList<TicketBookingRS>();
        List<Seat> newlyBookedSeats = seatRepository.saveAll(vacantSeats);
        newlyBookedSeats.stream().forEach(seat -> {
            TicketDetailsRS ticketDetails = new TicketDetailsRS(seat.getTicket().getTicketNumber(), seat.getSeatNumber(), seat.getTicket().getUpdatedAt());
            TicketBookingRS booking = new TicketBookingRS();
            booking.setTicketDetails(ticketDetails);
            results.add(booking);
        });
        bookedSeats.stream().forEach(seat -> {
            TicketBookingErrorMessage error = new TicketBookingErrorMessage(String.format(SEAT_NUMBER_ALREADY_BOOKED, seat.getSeatNumber()));
            TicketBookingRS booking = new TicketBookingRS();
            booking.setError(error);
            results.add(booking);
        });
        List<String> seatNumbers = eventSeats.stream().map(seat -> seat.getSeatNumber()).collect(Collectors.toList());
        List<String> nonExistentSeats = request.getSeats().stream().filter(item -> !seatNumbers.contains(item)).collect(Collectors.toList());
        nonExistentSeats.stream().forEach(seat -> {
            TicketBookingErrorMessage error = new TicketBookingErrorMessage(String.format(SEAT_NUMBER_NOT_EXISTING, seat));
            TicketBookingRS booking = new TicketBookingRS();
            booking.setError(error);
            results.add(booking);
        });
        return results;
    }

    public Ticket cancelTicket(CancelTicketRQ request) throws ResourceNotFoundException, UnauthorizedMethodException {
        Ticket ticket = ticketRepository.findByTicketNumberAndPhoneNumber(request.getTicketNumber(), request.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException(TICKET_NOT_FOUND));
        Seat seat = seatRepository.findByTicket(ticket)
                .orElseThrow(() -> new ResourceNotFoundException(SEAT_NOT_FOUND_FOR_THIS_TICKET));
        Long cancellationWindowInMinutes = ticket.getEvent().getCancellationWindowInMinutes();
        LocalDateTime time = LocalDateTime.ofInstant(ticket.getUpdatedAt().toInstant(), ZoneId.of("UTC"));
        LocalDateTime now = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
        if (time.plusMinutes(cancellationWindowInMinutes).isAfter(now) || time.plusMinutes(cancellationWindowInMinutes).isEqual(now)) {
            seat.setTicket(null);
            seatRepository.save(seat);
            ticketRepository.delete(ticket);
        } else {
            throw new UnauthorizedMethodException(CANCELLATION_WINDOW_ELAPSED);
        }
        return ticket;
    }

    public List<EventDetailRS> getEventDetails(Long eventNumber) throws ResourceNotFoundException {
        Event event = eventRepository.findByEventNumber(eventNumber)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND));
        List<Ticket> tickets = event.getTicket();
        return tickets.stream().map(ticket ->
                new EventDetailRS(eventNumber, ticket.getTicketNumber(), ticket.getPhoneNumber(), ticket.getSeat().getSeatNumber()))
                .collect(Collectors.toList());
    }
}
