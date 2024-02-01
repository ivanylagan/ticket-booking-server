package org.jpmc.ticketbookingserver.api.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TicketRQ {
    private List<String> seats;
    private String phoneNumber;

    public TicketRQ(List<String> seats, String phoneNumber) {
        this.seats = seats;
        this.phoneNumber = phoneNumber;
    }

    public TicketRQ() {}

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<String> getSeats() {
        return seats;
    }

    public void setSeats(List<String> seats) {
        this.seats = seats;
    }
}
