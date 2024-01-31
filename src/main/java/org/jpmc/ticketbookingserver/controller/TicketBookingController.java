package org.jpmc.ticketbookingserver.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.jpmc.ticketbookingserver.api.request.CancelTicketRQ;
import org.jpmc.ticketbookingserver.api.request.EventRQ;
import org.jpmc.ticketbookingserver.api.request.TicketRQ;
import org.jpmc.ticketbookingserver.api.response.EventDetailRS;
import org.jpmc.ticketbookingserver.api.response.EventRS;
import org.jpmc.ticketbookingserver.api.response.TicketBookingRS;
import org.jpmc.ticketbookingserver.entity.Event;
import org.jpmc.ticketbookingserver.entity.Seat;
import org.jpmc.ticketbookingserver.exception.ResourceDuplicateException;
import org.jpmc.ticketbookingserver.exception.ResourceNotFoundException;
import org.jpmc.ticketbookingserver.exception.UnauthorizedMethodException;
import org.jpmc.ticketbookingserver.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class TicketBookingController {

    @Autowired
    BookingService bookingService;

    @PostMapping(value = "/admin/events", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<EventRS> setupEvent(@Valid @RequestBody EventRQ request) throws ResourceDuplicateException {
        Event result = bookingService.createEvent(request);
        EventRS eventRS = new EventRS();
        eventRS.setEventNumber(result.getEventNumber());
        return ResponseEntity.ok().body(eventRS);
    }

    @GetMapping(value = "/admin/events/{eventNumber}/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EventDetailRS>> viewEventDetails(@PathVariable @Min(1) Long eventNumber)
            throws ResourceNotFoundException {
        return ResponseEntity.ok().body(bookingService.getEventDetails(eventNumber));
    }

    @GetMapping(value = "/events/{eventNumber}/available-seats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> viewAvailableSeats(@PathVariable @Min(1) Long eventNumber) throws ResourceNotFoundException {
        return ResponseEntity.ok().body(bookingService.getAvailableSeats(eventNumber));
    }

    @PostMapping(value = "/events/{eventNumber}/book", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<TicketBookingRS>> bookSeats(@Valid @RequestBody TicketRQ request,
                                                    @PathVariable @Min(1) Long eventNumber)
            throws ResourceNotFoundException {
        List<TicketBookingRS> seats = bookingService.bookSeats(request, eventNumber);


        return null;
//        return ResponseEntity.ok().body(seats.stream().map(seat -> new TicketRS(seat.getTicket().getTicketNumber(), seat.getSeatNumber())).collect(Collectors.toList()));
    }

    @DeleteMapping(value = "/events/cancel", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<EventRS> cancelTicket(@Valid @RequestBody CancelTicketRQ request)
            throws ResourceNotFoundException, UnauthorizedMethodException {
        bookingService.cancelTicket(request);
        return null;
    }

}
