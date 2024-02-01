package org.jpmc.ticketbookingserver.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.Date;


@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TicketBookingErrorMessage {


    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    public TicketBookingErrorMessage(String message) {
        this.message = message;

    }

    public String getMessage() {
        return message;
    }


}
