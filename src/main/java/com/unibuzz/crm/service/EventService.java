package com.unibuzz.crm.service;

import com.unibuzz.crm.entity.Event;
import com.unibuzz.crm.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EmailService emailService;
    private final com.unibuzz.crm.repository.UserRepository userRepository;
    private final com.unibuzz.crm.repository.RegistrationRepository registrationRepository;

    public boolean isEventExpired(Event event) {
        if (event == null || event.getEventDate() == null) return false;
        LocalDate today = LocalDate.now();
        if (event.getEventDate().isBefore(today)) return true;
        if (event.getEventDate().isEqual(today) && event.getEventTime() != null && !event.getEventTime().isBlank()) {
            try {
                String timeStr = event.getEventTime().trim().toUpperCase();
                int hour = 0;
                int minute = 0;
                if (timeStr.contains("AM") || timeStr.contains("PM")) {
                    boolean isPm = timeStr.contains("PM");
                    String clean = timeStr.replace("AM", "").replace("PM", "").trim();
                    String[] parts = clean.split(":");
                    hour = Integer.parseInt(parts[0].trim());
                    minute = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
                    if (isPm && hour < 12) hour += 12;
                    if (!isPm && hour == 12) hour = 0;
                } else {
                    String[] parts = timeStr.split(":");
                    hour = Integer.parseInt(parts[0].trim());
                    minute = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
                }
                java.time.LocalTime eventTime = java.time.LocalTime.of(hour, minute);
                return java.time.LocalTime.now().isAfter(eventTime);
            } catch (Exception ignored) {}
        }
        return false;
    }

    @Transactional
    public void cleanupExpiredEvents() {
        // Events and registrations are preserved in DB for attendance and certificate history.
        // Active vs past filtering is dynamically determined by isEventExpired.
    }

    private String formatTime12Hour(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) return "";
        if (timeStr.toUpperCase().contains("AM") || timeStr.toUpperCase().contains("PM")) {
            return timeStr;
        }
        try {
            String[] parts = timeStr.split(":");
            if (parts.length >= 2) {
                int hour = Integer.parseInt(parts[0].trim());
                String minute = parts[1].trim();
                String ampm = hour >= 12 ? "PM" : "AM";
                hour = hour % 12;
                if (hour == 0) hour = 12;
                return String.format("%d:%s %s", hour, minute, ampm);
            }
        } catch (Exception ignored) {}
        return timeStr;
    }

    private void validateEventDateTime(LocalDate eventDate, String eventTime) {
        if (eventDate == null) {
            throw new RuntimeException("Event date is required.");
        }
        LocalDate today = LocalDate.now();
        if (eventDate.isBefore(today)) {
            throw new RuntimeException("Event date cannot be in the past.");
        }
        if (eventDate.isEqual(today) && eventTime != null && !eventTime.isBlank()) {
            try {
                String[] parts = eventTime.split(":");
                if (parts.length >= 2) {
                    int hour = Integer.parseInt(parts[0].trim());
                    int minute = Integer.parseInt(parts[1].trim());
                    java.time.LocalTime time = java.time.LocalTime.of(hour, minute);
                    if (java.time.LocalTime.now().isAfter(time)) {
                        throw new RuntimeException("Event time cannot be in the past for today's date.");
                    }
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    public Event createEvent(Event event, String facultyEmail) {
        validateEventDateTime(event.getEventDate(), event.getEventTime());
        event.setFacultyEmail(facultyEmail);
        Event savedEvent = eventRepository.save(event);

        String formattedTime = formatTime12Hour(savedEvent.getEventTime());
        String dateWithTime = savedEvent.getEventDate().toString() + (!formattedTime.isBlank() ? " at " + formattedTime : "");

        // Notify all students
        userRepository.findByRoles_Name("STUDENT").forEach(student -> {
            emailService.sendNewEventNotification(
                student.getEmail(),
                savedEvent.getTitle(),
                dateWithTime,
                savedEvent.getLocation()
            );
        });

        return savedEvent;
    }

    @Transactional
    public List<Event> getAllEvents() {
        cleanupExpiredEvents();
        return eventRepository.findAll();
    }

    @Transactional
    public List<Event> getMyEvents(String email) {
        cleanupExpiredEvents();
        return eventRepository.findByFacultyEmail(email);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id).orElse(null);
        if (event != null) {
            String eventTitle = event.getTitle();
            registrationRepository.findByEventId(id).forEach(registration -> {
                emailService.sendEventCancellationNotification(
                    registration.getStudentEmail(),
                    eventTitle
                );
            });
            registrationRepository.deleteByEventId(id);
            eventRepository.delete(event);
        }
    }

    public Event updateEvent(Long id, Event updatedEvent) {
        validateEventDateTime(updatedEvent.getEventDate(), updatedEvent.getEventTime());
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        existingEvent.setTitle(updatedEvent.getTitle());
        existingEvent.setDescription(updatedEvent.getDescription());
        existingEvent.setEventDate(updatedEvent.getEventDate());
        existingEvent.setEventTime(updatedEvent.getEventTime());
        existingEvent.setLocation(updatedEvent.getLocation());
        existingEvent.setSeats(updatedEvent.getSeats());
        existingEvent.setCategory(updatedEvent.getCategory());

        Event savedEvent = eventRepository.save(existingEvent);

        String formattedTime = formatTime12Hour(savedEvent.getEventTime());
        String dateWithTime = savedEvent.getEventDate().toString() + (!formattedTime.isBlank() ? " at " + formattedTime : "");

        // Notify all registered students
        registrationRepository.findByEventId(id).forEach(registration -> {
            emailService.sendEventUpdateNotification(
                registration.getStudentEmail(),
                savedEvent.getTitle(),
                dateWithTime,
                savedEvent.getLocation(),
                savedEvent.getDescription(),
                savedEvent.getCategory()
            );
        });

        return savedEvent;
    }
}
