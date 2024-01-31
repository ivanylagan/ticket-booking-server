package org.jpmc.ticketbookingserver.api.response;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class EventRS {
    private Long eventNumber;

    public Long getEventNumber() {
        return eventNumber;
    }

    public void setEventNumber(Long eventNumber) {
        this.eventNumber = eventNumber;
    }
}
