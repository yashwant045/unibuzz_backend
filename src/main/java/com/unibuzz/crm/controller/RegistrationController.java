package com.unibuzz.crm.controller;

import com.unibuzz.crm.entity.Registration;
import com.unibuzz.crm.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import com.unibuzz.crm.dto.RegistrationResponseDTO;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/{eventId}")
    public ResponseEntity<String> register(
            @PathVariable Long eventId,
            Authentication auth
    ) {
        registrationService.register(auth.getName(), eventId);
        return ResponseEntity.ok("Registered successfully");
    }

    @GetMapping("/my")
    public List<RegistrationResponseDTO> myRegistrations(Authentication auth) {
        return registrationService.getByStudent(auth.getName()).stream()
                .map(reg -> RegistrationResponseDTO.builder()
                        .id(reg.getId())
                        .eventId(reg.getEventId())
                        .studentEmail(reg.getStudentEmail())
                        .attended(reg.isAttended())
                        .build())
                .collect(Collectors.toList());
    }

    @GetMapping("/event/{eventId}")
    public List<RegistrationResponseDTO> eventRegistrations(@PathVariable Long eventId) {
        return registrationService.getByEvent(eventId).stream()
                .map(reg -> RegistrationResponseDTO.builder()
                        .id(reg.getId())
                        .eventId(reg.getEventId())
                        .studentEmail(reg.getStudentEmail())
                        .attended(reg.isAttended())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Faculty toggles attendance for a student on an event.
     * PUT /api/registrations/{eventId}/attend?studentEmail=...
     * Returns: { "attended": true/false }
     */
    @PutMapping("/{eventId}/attend")
    public ResponseEntity<java.util.Map<String, Boolean>> toggleAttendance(
            @PathVariable Long eventId,
            @RequestParam String studentEmail
    ) {
        boolean newState = registrationService.toggleAttendance(studentEmail, eventId);
        return ResponseEntity.ok(java.util.Map.of("attended", newState));
    }
}
