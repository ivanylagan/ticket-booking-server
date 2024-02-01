package org.jpmc.ticketbookingserver.repository;

import org.jpmc.ticketbookingserver.entity.Event;
import org.jpmc.ticketbookingserver.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketNumberAndPhoneNumber(String ticketNumber, String phoneNumber);
    boolean existsByEventAndPhoneNumber(Event event, String phoneNumber);

}
