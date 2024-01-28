package org.jpmc.ticketbookingserver.api.response;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class EventDetailRS {
    private Long eventNumber;
    private String ticketNumber;
    private String phoneNumber;
    private String seatNumber;

    public EventDetailRS(Long eventNumber, String ticketNumber, String phoneNumber, String seatNumber) {
        this.eventNumber = eventNumber;
        this.ticketNumber = ticketNumber;
        this.phoneNumber = phoneNumber;
        this.seatNumber = seatNumber;
    }

    public Long getEventNumber() {
        return eventNumber;
    }

    public void setEventNumber(Long eventNumber) {
        this.eventNumber = eventNumber;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }
}
