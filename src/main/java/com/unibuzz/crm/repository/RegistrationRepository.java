package com.unibuzz.crm.repository;

import com.unibuzz.crm.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    List<Registration> findByStudentEmail(String email);

    List<Registration> findByEventId(Long eventId);

    boolean existsByStudentEmailAndEventId(String email, Long eventId);

    /** Returns only registrations where the student has been marked as attended. */
    List<Registration> findByStudentEmailAndAttendedTrue(String email);

    /** Used for mark-attendance lookups. */
    Optional<Registration> findByStudentEmailAndEventId(String email, Long eventId);

    void deleteByEventId(Long eventId);
}
