package com.unibuzz.crm.repository;

import com.unibuzz.crm.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByFacultyEmail(String facultyEmail);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT e FROM Event e WHERE e.id = :id")
    java.util.Optional<Event> findByIdWithLock(@org.springframework.data.repository.query.Param("id") Long id);

    List<Event> findByEventDate(java.time.LocalDate eventDate);

    List<Event> findByEventDateBefore(java.time.LocalDate date);
}
