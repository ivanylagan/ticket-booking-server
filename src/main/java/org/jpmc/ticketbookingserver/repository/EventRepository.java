package org.jpmc.ticketbookingserver.repository;

import org.jpmc.ticketbookingserver.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByEventNumber(Long eventNumber);
}
