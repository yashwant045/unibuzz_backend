package com.unibuzz.crm.controller;

import com.unibuzz.crm.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    /**
     * GET /api/certificates/download/{registrationId}
     *
     * Generates and streams a PDF certificate for the authenticated student.
     * Only the owner of the registration (or an admin) may download it.
     */
    @GetMapping("/download/{registrationId}")
    public ResponseEntity<byte[]> downloadCertificate(
            @PathVariable Long registrationId,
            Authentication auth
    ) {
        byte[] pdf = certificateService.generateCertificate(registrationId, auth.getName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData(
                "attachment",
                "certificate_" + registrationId + ".pdf"
        );
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setContentLength(pdf.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}
