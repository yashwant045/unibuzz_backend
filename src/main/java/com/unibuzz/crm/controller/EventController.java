package com.unibuzz.crm.controller;

import com.unibuzz.crm.dto.CreateEventRequest;
import com.unibuzz.crm.entity.Event;
import com.unibuzz.crm.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public ResponseEntity<Event> createEvent(
            @RequestBody CreateEventRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .eventTime(request.getEventTime())
                .location(request.getLocation())
                .seats(request.getSeats())
                .category(request.getCategory())
                .build();
        return ResponseEntity.ok(eventService.createEvent(event, email));
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<List<Event>> getMyEvents(Authentication authentication) {
        return ResponseEntity.ok(
                eventService.getMyEvents(authentication.getName())
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public ResponseEntity<String> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok("Event deleted");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public ResponseEntity<Event> updateEvent(
            @PathVariable Long id,
            @RequestBody CreateEventRequest request
    ) {
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .eventTime(request.getEventTime())
                .location(request.getLocation())
                .seats(request.getSeats())
                .category(request.getCategory())
                .build();
        return ResponseEntity.ok(eventService.updateEvent(id, event));
    }
}
