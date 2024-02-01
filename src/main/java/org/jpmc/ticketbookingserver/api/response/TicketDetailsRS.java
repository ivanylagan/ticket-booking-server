package org.jpmc.ticketbookingserver.api.response;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.util.Date;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TicketDetailsRS {
    private String ticketNumber;
    private String seatNumber;
    private Date bookingTimestamp;

    public TicketDetailsRS(String ticketNumber, String seatNumber, Date bookingTimestamp) {
        this.ticketNumber = ticketNumber;
        this.seatNumber = seatNumber;
        this.bookingTimestamp = bookingTimestamp;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Date getBookingTimestamp() {
        return bookingTimestamp;
    }

    public void setBookingTimestamp(Date bookingTimestamp) {
        this.bookingTimestamp = bookingTimestamp;
    }
}
