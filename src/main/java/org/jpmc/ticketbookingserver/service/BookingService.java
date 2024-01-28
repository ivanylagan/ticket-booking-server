package org.jpmc.ticketbookingserver.service;

import jakarta.transaction.Transactional;
import org.jpmc.ticketbookingserver.api.request.CancelTicketRQ;
import org.jpmc.ticketbookingserver.api.request.EventRQ;
import org.jpmc.ticketbookingserver.api.request.TicketRQ;
import org.jpmc.ticketbookingserver.api.response.EventDetailRS;
import org.jpmc.ticketbookingserver.entity.Event;
import org.jpmc.ticketbookingserver.entity.Seat;
import org.jpmc.ticketbookingserver.entity.Ticket;
import org.jpmc.ticketbookingserver.entity.User;
import org.jpmc.ticketbookingserver.repository.EventRepository;
import org.jpmc.ticketbookingserver.repository.SeatRepository;
import org.jpmc.ticketbookingserver.repository.TicketRepository;
import org.jpmc.ticketbookingserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("bookingService")
@Transactional
public class BookingService {

    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final String ROWS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Autowired
    BookingService(SeatRepository seatRepository, EventRepository eventRepository, UserRepository userRepository, TicketRepository ticketRepository) {
        this.seatRepository = seatRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
    }

    public Event create(EventRQ eventRQ) {
        if (eventRepository.existsById(eventRQ.getEventNumber())) {
            // TODO throw new exception
        }
        Event event = new Event();
        event.setEventNumber(eventRQ.getEventNumber());
        event.setRowCount(eventRQ.getRowCount());
        event.setSeatsPerRowCount(eventRQ.getSeatsPerRow());
        event.setCancellationWindowInMinutes(eventRQ.getCancellationWindow());
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

    public List<String> getAvailableSeats(Long eventNumber) {
        List<Seat> seats = seatRepository.findAllByEventIdAndTicketIsNull(eventNumber);
        return seats.stream().map(seat -> seat.getSeatNumber()).collect(Collectors.toList());
    }

    public List<Seat> bookSeats(TicketRQ request, Long eventNumber) {
        Event event = eventRepository.findById(eventNumber).orElse(null);
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber()).orElse(null);
        List<Ticket> tickets = new ArrayList<Ticket>();
        if (event == null) {
            // TODO throw new exception
        }
        if (user == null) {
            // TODO throw new exception
        }
        List<Seat> seats = seatRepository.findAllByEventIdAndTicketIsNullAndSeatNumberIn(eventNumber, request.getSeats());
        for (Seat seat : seats) {
            Ticket ticket = new Ticket();
            ticket.setSeat(seat);
            ticket.setEvent(event);
            ticket.setUpdatedAt(new Timestamp(Instant.now().toEpochMilli()));
            ticket.setTicketNumber(Long.toString(event.getEventNumber()) + seat.getSeatNumber());
            ticket.setUser(user);
            seat.setTicket(ticket);
        }
        List<Seat> results = seatRepository.saveAll(seats);
        return results;
    }

    public Ticket cancelTicket(CancelTicketRQ request) {
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber()).orElse(null);
        Ticket ticket = ticketRepository.findByTicketNumber(request.getTicketNumber()).orElse(null);
        Seat seat = seatRepository.findByTicket(ticket).orElse(null);
        if (ticket == null) {
            // TODO throw new exception
        }
        if (seat == null) {
            // TODO throw new exception
        }
        if (user == null) {
            // TODO throw new exception
        }
        seat.setTicket(null);
        seatRepository.save(seat);
        ticketRepository.delete(ticket);
        return ticket;
    }

    public List<EventDetailRS> getEventDetails(Long eventNumber) {
            List<EventDetailRS> eventDetailRS = new ArrayList<EventDetailRS>();
            Event event = eventRepository.findByEventNumber(eventNumber).orElse(null);
            if (event == null) {
                // TODO throw new exception
            }
            List<Ticket> tickets = event.getTicket();
            for (Ticket ticket : tickets) {
                eventDetailRS.add(new EventDetailRS(eventNumber, ticket.getTicketNumber(), ticket.getUser().getPhoneNumber(), ticket.getSeat().getSeatNumber()));
            }
        return eventDetailRS;
    }
}
