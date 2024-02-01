package org.jpmc.ticketbookingserver.service;

import org.aspectj.lang.annotation.Before;
import org.jpmc.ticketbookingserver.api.request.CancelTicketRQ;
import org.jpmc.ticketbookingserver.api.request.EventRQ;
import org.jpmc.ticketbookingserver.api.request.TicketRQ;
import org.jpmc.ticketbookingserver.api.response.TicketBookingRS;
import org.jpmc.ticketbookingserver.entity.Event;
import org.jpmc.ticketbookingserver.entity.Seat;
import org.jpmc.ticketbookingserver.entity.Ticket;
import org.jpmc.ticketbookingserver.exception.ResourceDuplicateException;
import org.jpmc.ticketbookingserver.exception.ResourceNotFoundException;
import org.jpmc.ticketbookingserver.exception.UnauthorizedMethodException;
import org.jpmc.ticketbookingserver.repository.EventRepository;
import org.jpmc.ticketbookingserver.repository.SeatRepository;
import org.jpmc.ticketbookingserver.repository.TicketRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
public class BookingServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private TicketRepository ticketRepository;

    @Before("")
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = ResourceDuplicateException.class)
    public void testCreateDuplicateEvent() throws ResourceDuplicateException {
        EventRQ request = new EventRQ(1L, 26, 10, 1L);
        when(eventRepository.existsByEventNumber(anyLong())).thenReturn(true);
        bookingService.createEvent(request);
    }

    @Test
    public void testCreateNewEvent() throws ResourceDuplicateException {
        EventRQ request = new EventRQ(1L, 2, 2, 1L);

        List<Seat> seats = new ArrayList<Seat>();
        Event event = new Event(request);
        seats.add(new Seat(event, "A1"));
        seats.add(new Seat(event, "A2"));
        seats.add(new Seat(event, "B1"));
        seats.add(new Seat(event, "B2"));
        event.setSeats(seats);

        when(eventRepository.existsByEventNumber(anyLong())).thenReturn(false);
        when(eventRepository.save(any())).thenReturn(event);

        Event result = bookingService.createEvent(request);
        assertEquals(4, result.getSeats().size());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetAvailableSeatsThrowEventNotFound() throws ResourceNotFoundException {
        when(eventRepository.findByEventNumber(anyLong())).thenReturn(Optional.empty());
        bookingService.getAvailableSeats(1L);
    }

    @Test
    public void testGetAvailableSeats() throws ResourceNotFoundException {
        EventRQ request = new EventRQ(1L, 1, 2, 1L);
        Event event = new Event(request);
        List<Seat> seats = new ArrayList<Seat>();
        seats.add(new Seat(event, "A1"));
        seats.add(new Seat(event, "A2"));
        event.setSeats(seats);

        when(eventRepository.findByEventNumber(anyLong())).thenReturn(Optional.of(event));
        when(seatRepository.findAllByEventAndTicketIsNull(event)).thenReturn(seats);

        List<String> results = bookingService.getAvailableSeats(1L);

        assertEquals(2, results.size());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testBookSeatsWhenEventIsNotFound() throws ResourceNotFoundException, ResourceDuplicateException {
        TicketRQ request = new TicketRQ(Arrays.asList("A1", "A2"), "09958515295");

        when(eventRepository.findByEventNumber(anyLong())).thenReturn(Optional.empty());
        bookingService.bookSeats(request, 1L);

    }

    @Test(expected = ResourceDuplicateException.class)
    public void testBookSeatsWhenTicketDoesNotExists() throws ResourceNotFoundException, ResourceDuplicateException {
        EventRQ eventRequest = new EventRQ(1L, 2, 2, 1L);
        Event event = new Event(eventRequest);

        TicketRQ ticketRequest = new TicketRQ(Arrays.asList("A1", "A2"), "09958515295");

        when(eventRepository.findByEventNumber(anyLong())).thenReturn(Optional.of(event));
        when(ticketRepository.existsByEventAndPhoneNumber(any(), anyString())).thenReturn(true);

        bookingService.bookSeats(ticketRequest, 1L);

    }

    @Test
    public void testBookSeatsWhenOneSeatNumberIsAlreadyBooked() throws ResourceNotFoundException, ResourceDuplicateException {
        EventRQ eventRequest = new EventRQ(1L, 2, 2, 1L);

        List<Seat> seats = new ArrayList<Seat>();
        Event event = new Event(eventRequest);
        Seat vacantSeat = new Seat(event, "A1");
        vacantSeat.setTicket(null);
        seats.add(vacantSeat);
        Seat bookedSeat = new Seat(event, "A2");
        Ticket bookedTicket = new Ticket();
        bookedTicket.setId(1L);
        bookedTicket.setTicketNumber("1A2");
        bookedSeat.setEvent(event);
        bookedSeat.setTicket(bookedTicket);
        seats.add(bookedSeat);
        event.setSeats(seats);

        TicketRQ ticketRequest = new TicketRQ(Arrays.asList("A1", "A2"), "09958515295");

        when(eventRepository.findByEventNumber(anyLong())).thenReturn(Optional.of(event));
        when(ticketRepository.existsByEventAndPhoneNumber(any(), anyString())).thenReturn(false);
        when(seatRepository.findAllByEventAndSeatNumberIn(any(), anyList())).thenReturn(seats);
        Seat newSeat = new Seat(event, "A1");
        Ticket newTicket = new Ticket();
        newTicket.setId(2L);
        newTicket.setTicketNumber("1A1");
        newSeat.setTicket(newTicket);
        newSeat.setId(2L);
        newSeat.setEvent(event);

        when(seatRepository.saveAll(anyList())).thenReturn(Arrays.asList(newSeat));

        List<TicketBookingRS> results = bookingService.bookSeats(ticketRequest, 1L);

        assertEquals(2, results.size());
        assertEquals("Seat number A2 already booked.", results.get(1).getError().getMessage());

    }


    @Test(expected = ResourceNotFoundException.class)
    public void testCancelTicketIfTicketDoesNotExists() throws ResourceNotFoundException, UnauthorizedMethodException {
        CancelTicketRQ cancelRequest = new CancelTicketRQ("09958515295", "1A1");

        when(ticketRepository.findByTicketNumberAndPhoneNumber(anyString(), anyString())).thenReturn(Optional.empty());

        bookingService.cancelTicket(cancelRequest);
    }


    @Test(expected = ResourceNotFoundException.class)
    public void testCancelTicketIfSeatIsNotLinkedToTicket() throws ResourceNotFoundException, UnauthorizedMethodException {
        CancelTicketRQ cancelRequest = new CancelTicketRQ("09958515295", "1A1");

        when(ticketRepository.findByTicketNumberAndPhoneNumber(anyString(), anyString())).thenReturn(Optional.of(new Ticket()));
        when(seatRepository.findByTicket(any())).thenReturn(Optional.empty());

        bookingService.cancelTicket(cancelRequest);
    }

    @Test(expected = UnauthorizedMethodException.class)
    public void testCancelTicketIfCancellationWindowElapsed() throws ResourceNotFoundException, UnauthorizedMethodException {
        EventRQ eventRequest = new EventRQ(1L, 2, 2, 1L);
        Event event = new Event(eventRequest);

        Ticket newTicket = new Ticket();
        newTicket.setId(1L);
        newTicket.setTicketNumber("1A1");
        newTicket.setUpdatedAt(Timestamp.from(Instant.ofEpochMilli(1706752800000L)));
        newTicket.setEvent(event);
        Seat newSeat = new Seat(event, "A1");
        newSeat.setTicket(newTicket);
        newSeat.setId(2L);
        newSeat.setEvent(event);

        CancelTicketRQ cancelRequest = new CancelTicketRQ("09958515295", "1A1");

        when(ticketRepository.findByTicketNumberAndPhoneNumber(anyString(), anyString())).thenReturn(Optional.of(newTicket));
        when(seatRepository.findByTicket(any())).thenReturn(Optional.of(newSeat));

        bookingService.cancelTicket(cancelRequest);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetEventDetailsWhenEventDoesNotExists() throws ResourceNotFoundException {
        EventRQ request = new EventRQ(1L, 26, 10, 1L);
        when(eventRepository.findByEventNumber(anyLong())).thenReturn(Optional.empty());
        bookingService.getEventDetails(1L);
    }


}
