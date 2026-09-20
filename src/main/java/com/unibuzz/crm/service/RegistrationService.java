package com.unibuzz.crm.service;

import com.unibuzz.crm.entity.Event;
import com.unibuzz.crm.entity.Registration;
import com.unibuzz.crm.repository.EventRepository;
import com.unibuzz.crm.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;

    @Transactional
    public void register(String email, Long eventId) {
        boolean exists = registrationRepository.existsByStudentEmailAndEventId(email, eventId);
        if (exists) {
            throw new RuntimeException("You are already registered for this event.");
        }

        Event event = eventRepository.findByIdWithLock(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        int registeredCount = event.getRegisteredCount() == null ? 0 : event.getRegisteredCount();
        int seats = event.getSeats() == null ? 0 : event.getSeats();
        if (registeredCount >= seats) {
            throw new RuntimeException("Event is fully booked! No seats available.");
        }

        Registration reg = Registration.builder()
                .studentEmail(email)
                .eventId(eventId)
                .build();

        registrationRepository.save(reg);

        event.setRegisteredCount(registeredCount + 1);
        eventRepository.save(event);
    }

    public List<Registration> getByStudent(String email) {
        return registrationRepository.findByStudentEmail(email);
    }

    public List<Registration> getByEvent(Long eventId) {
        return registrationRepository.findByEventId(eventId);
    }

    /** Returns only registrations where attended = true. */
    public List<Registration> getAttendedByStudent(String email) {
        return registrationRepository.findByStudentEmailAndAttendedTrue(email);
    }

    /** Toggles the attended flag for a student on a given event. */
    @Transactional
    public boolean toggleAttendance(String studentEmail, Long eventId) {
        Registration reg = registrationRepository
                .findByStudentEmailAndEventId(studentEmail, eventId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));
        reg.setAttended(!reg.isAttended());
        registrationRepository.save(reg);
        return reg.isAttended();
    }
}
