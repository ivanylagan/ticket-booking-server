package org.jpmc.ticketbookingserver.constants;

public class ErrorMessageConstant {

    public static final String SEAT_NUMBER_ALREADY_BOOKED = "Seat number %s already booked.";
    public static final String SEAT_NUMBER_NOT_EXISTING = "Seat number %s does not exist.";
    public static final String CANCELLATION_WINDOW_ELAPSED = "Cancellation window has elapsed. Ticket cannot be cancelled.";
    public static final String SEAT_NOT_FOUND_FOR_THIS_TICKET = "seat not found associated to the ticket.";
    public static final String TICKET_NOT_FOUND = "ticket not found in the system.";
    public static final String EVENT_NOT_FOUND = "event not found";
    public static final String EVENT_ALREADY_EXISTS = "event already existing.";
    public static final String TICKET_ALREADY_BOOKED_BY_ANOTHER_BUYER = "Existing ticket already booked by the buyer for the said event.";
}
