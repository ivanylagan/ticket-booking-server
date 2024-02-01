package org.jpmc.ticketbookingserver.repository;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.jpmc.ticketbookingserver.entity.Event;
import org.jpmc.ticketbookingserver.entity.Seat;
import org.jpmc.ticketbookingserver.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    public List<Seat> findAllByEventAndTicketIsNull(Event event);
    public List<Seat> findAllByEventAndSeatNumberIn(Event event, List<String> seatNumber);
    public Optional<Seat> findByTicket(Ticket ticket);

}
