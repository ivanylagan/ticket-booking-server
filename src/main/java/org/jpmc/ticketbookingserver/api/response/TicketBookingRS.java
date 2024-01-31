package org.jpmc.ticketbookingserver.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;


@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TicketBookingRS {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    TicketDetailsRS ticketDetails;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    ErrorMessage error;

    public TicketDetailsRS getTicketDetails() {
        return ticketDetails;
    }

    public void setTicketDetails(TicketDetailsRS ticketDetails) {
        this.ticketDetails = ticketDetails;
    }

    public ErrorMessage getError() {
        return error;
    }

    public void setError(ErrorMessage error) {
        this.error = error;
    }
}
