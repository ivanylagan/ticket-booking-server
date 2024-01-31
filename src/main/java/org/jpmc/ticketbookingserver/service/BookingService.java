package org.jpmc.ticketbookingserver.service;

import jakarta.transaction.Transactional;
import org.jpmc.ticketbookingserver.api.request.CancelTicketRQ;
import org.jpmc.ticketbookingserver.api.request.EventRQ;
import org.jpmc.ticketbookingserver.api.request.TicketRQ;
import org.jpmc.ticketbookingserver.api.response.EventDetailRS;
import org.jpmc.ticketbookingserver.api.response.TicketBookingRS;
import org.jpmc.ticketbookingserver.entity.Event;
import org.jpmc.ticketbookingserver.entity.Seat;
import org.jpmc.ticketbookingserver.entity.Ticket;
import org.jpmc.ticketbookingserver.entity.User;
import org.jpmc.ticketbookingserver.exception.ResourceDuplicateException;
import org.jpmc.ticketbookingserver.exception.ResourceNotFoundException;
import org.jpmc.ticketbookingserver.exception.UnauthorizedMethodException;
import org.jpmc.ticketbookingserver.repository.EventRepository;
import org.jpmc.ticketbookingserver.repository.SeatRepository;
import org.jpmc.ticketbookingserver.repository.TicketRepository;
import org.jpmc.ticketbookingserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
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

    public Event createEvent(EventRQ eventRQ) throws ResourceDuplicateException {
        if (eventRepository.existsById(eventRQ.getEventNumber())) {
            throw new ResourceDuplicateException("event already existing.");
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
        if (!eventRepository.existsById(eventNumber)) {
            throw new ResourceNotFoundException("event not found.");
        }
        List<Seat> seats = seatRepository.findAllByEventIdAndTicketIsNull(eventNumber);
        return seats.stream().map(seat -> seat.getSeatNumber()).collect(Collectors.toList());
    }

    public List<TicketBookingRS> bookSeats(TicketRQ request, Long eventNumber) throws ResourceNotFoundException {
        Event event = eventRepository.findById(eventNumber)
                .orElseThrow(() -> new ResourceNotFoundException("event not found"));
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));
        List<Ticket> tickets = new ArrayList<Ticket>();
        List<Seat> seats = seatRepository.findAllByEventIdAndSeatNumberIn(eventNumber, request.getSeats());





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


        // user already booked the designated seat for the given event
        // booked seats for given event not booked by current user
        // seats not included in the event

        List<TicketBookingRS> s = (List<TicketBookingRS>) Collectors.toList();

        return s;
    }

    public Ticket cancelTicket(CancelTicketRQ request) throws ResourceNotFoundException, UnauthorizedMethodException {
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("user not found."));
        Ticket ticket = ticketRepository.findByTicketNumberAndUser(request.getTicketNumber(), user)
                .orElseThrow(() -> new ResourceNotFoundException("ticket not found in the system."));
        Seat seat = seatRepository.findByTicket(ticket)
                .orElseThrow(() -> new ResourceNotFoundException("seat not found associated to the ticket."));
        Long cancellationWindowInMinutes = ticket.getEvent().getCancellationWindowInMinutes();
        LocalDateTime time = LocalDateTime.ofInstant(ticket.getUpdatedAt().toInstant(), ZoneId.of("UTC"));
        LocalDateTime now = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
        if (time.plusMinutes(cancellationWindowInMinutes).isAfter(now) || time.plusMinutes(cancellationWindowInMinutes).isEqual(now)) {
            seat.setTicket(null);
            seatRepository.save(seat);
            ticketRepository.delete(ticket);
        } else {
            throw new UnauthorizedMethodException("Cancellation window has elapsed. Ticket cannot be cancelled.");
        }
        return ticket;
    }

    public List<EventDetailRS> getEventDetails(Long eventNumber) throws ResourceNotFoundException {
            List<EventDetailRS> eventDetailRS = new ArrayList<EventDetailRS>();
            Event event = eventRepository.findByEventNumber(eventNumber).orElse(null);
            if (event == null) {
                throw new ResourceNotFoundException("event not found");
            }
            List<Ticket> tickets = event.getTicket();
            for (Ticket ticket : tickets) {
                eventDetailRS.add(new EventDetailRS(eventNumber, ticket.getTicketNumber(), ticket.getUser().getPhoneNumber(), ticket.getSeat().getSeatNumber()));
            }
        return eventDetailRS;
    }
}
