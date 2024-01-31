package org.jpmc.ticketbookingserver.entity;

import jakarta.persistence.*;
import org.jpmc.ticketbookingserver.api.request.EventRQ;

import java.util.List;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false, unique = true)
    private Long eventNumber;

    @Column(nullable = false)
    private Integer rowCount;

    @Column(nullable = false)
    private Integer seatsPerRowCount;

    @Column(nullable = false)
    private Long cancellationWindowInMinutes;

    public Event(EventRQ request) {
        this.eventNumber = request.getEventNumber();
        this.rowCount = request.getRowCount();
        this.seatsPerRowCount = request.getSeatsPerRow();
        this.cancellationWindowInMinutes = request.getCancellationWindow();
    }

    public Event() {}

    @OneToMany(mappedBy = "event")
    private List<Ticket> ticket;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Seat> seats;

    public List<Ticket> getTicket() {
        return ticket;
    }

    public void setTicket(List<Ticket> ticket) {
        this.ticket = ticket;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public Long getEventNumber() {
        return eventNumber;
    }

    public void setEventNumber(Long eventNumber) {
        this.eventNumber = eventNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    public Integer getSeatsPerRowCount() {
        return seatsPerRowCount;
    }

    public void setSeatsPerRowCount(Integer seatsPerRowCount) {
        this.seatsPerRowCount = seatsPerRowCount;
    }

    public Long getCancellationWindowInMinutes() {
        return cancellationWindowInMinutes;
    }

    public void setCancellationWindowInMinutes(Long cancellationWindowInMinutes) {
        this.cancellationWindowInMinutes = cancellationWindowInMinutes;
    }
}
