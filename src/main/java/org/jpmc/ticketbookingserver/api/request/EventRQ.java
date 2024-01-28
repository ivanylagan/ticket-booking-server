package org.jpmc.ticketbookingserver.api.request;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class EventRQ {

    @NotNull
    @Min(value = 1, message = "event number should be at least 1")
    private Long eventNumber;

    @NotNull
    @Min(value = 1, message = "row count should be at least 1")
    @Max(value = 26, message = "row count should be at most 26")
    private Integer rowCount;

    @NotNull
    @Min(value = 1, message = "seats per row should be at least 1")
    @Max(value = 10, message = "seats per row should be at most 10")
    private Integer seatsPerRow;

    private Long cancellationWindow;

    public EventRQ(Long eventNumber, Integer rowCount, Integer seatsPerRow, Long cancellationWindow) {
        this.eventNumber = eventNumber;
        this.rowCount = rowCount;
        this.seatsPerRow = seatsPerRow;
        this.cancellationWindow = cancellationWindow;
    }

    public Long getEventNumber() {
        return eventNumber;
    }

    public void setEventNumber(Long eventNumber) {
        this.eventNumber = eventNumber;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    public Integer getSeatsPerRow() {
        return seatsPerRow;
    }

    public void setSeatsPerRow(Integer seatsPerRow) {
        this.seatsPerRow = seatsPerRow;
    }

    public Long getCancellationWindow() {
        return cancellationWindow;
    }

    public void setCancellationWindow(Long cancellationWindow) {
        this.cancellationWindow = cancellationWindow;
    }
}
