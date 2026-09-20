package com.unibuzz.crm.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.unibuzz.crm.entity.Event;
import com.unibuzz.crm.entity.Registration;
import com.unibuzz.crm.repository.EventRepository;
import com.unibuzz.crm.repository.RegistrationRepository;
import com.unibuzz.crm.repository.UserRepository;
import com.unibuzz.crm.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    /**
     * Generates a PDF certificate in memory for the given registrationId.
     * Throws RuntimeException if the registration is not found or attendance is not marked.
     */
    public byte[] generateCertificate(Long registrationId, String requestingEmail) {

        // 1. Load registration
        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found: " + registrationId));

        // 2. Security: only the registered student can download
        if (!reg.getStudentEmail().equalsIgnoreCase(requestingEmail)) {
            throw new RuntimeException("Access denied");
        }

        // 3. Must have attended
        if (!reg.isAttended()) {
            throw new RuntimeException("Certificate not available: attendance not marked");
        }

        // 4. Load event
        Event event = eventRepository.findById(reg.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // 5. Load student name (fallback to email)
        String studentName = userRepository.findByEmail(reg.getStudentEmail())
                .map(User::getFullName)
                .orElse(reg.getStudentEmail());

        String eventTitle = event.getTitle() != null ? event.getTitle() : "N/A";
        String eventDate = event.getEventDate() != null
                ? event.getEventDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                : "N/A";

        return buildPdf(studentName, eventTitle, eventDate);
    }

    // ── PDF layout ────────────────────────────────────────────────────────────

    private byte[] buildPdf(String studentName, String eventTitle, String eventDate) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document doc = new Document(PageSize.A4.rotate()); // landscape
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            doc.open();

            PdfContentByte canvas = writer.getDirectContentUnder();

            // ── Background gradient (dark navy → indigo) ──────────────────────
            Color bgTop    = new Color(15, 23, 42);    // slate-900
            Color bgBottom = new Color(30, 27, 75);    // indigo-950
            PdfShading shading = PdfShading.simpleAxial(writer,
                    0, PageSize.A4.getHeight(),
                    0, 0,
                    bgTop, bgBottom);
            PdfShadingPattern shadingPattern = new PdfShadingPattern(shading);
            canvas.setShadingFill(shadingPattern);
            canvas.rectangle(0, 0, PageSize.A4.getHeight(), PageSize.A4.getWidth());
            canvas.fill();

            // ── Decorative border ─────────────────────────────────────────────
            canvas.setLineWidth(3f);
            canvas.setColorStroke(new Color(99, 102, 241)); // indigo-500
            float m = 30f;
            canvas.rectangle(m, m,
                    PageSize.A4.getHeight() - 2 * m,
                    PageSize.A4.getWidth() - 2 * m);
            canvas.stroke();

            // inner thin border
            canvas.setLineWidth(1f);
            canvas.setColorStroke(new Color(165, 180, 252)); // indigo-300
            float m2 = 38f;
            canvas.rectangle(m2, m2,
                    PageSize.A4.getHeight() - 2 * m2,
                    PageSize.A4.getWidth() - 2 * m2);
            canvas.stroke();

            // ── Corner ornament dots ──────────────────────────────────────────
            canvas.setColorFill(new Color(99, 102, 241));
            float[] corners = {m, m,
                    PageSize.A4.getHeight() - m, m,
                    m, PageSize.A4.getWidth() - m,
                    PageSize.A4.getHeight() - m, PageSize.A4.getWidth() - m};
            for (int i = 0; i < corners.length; i += 2) {
                canvas.circle(corners[i], corners[i + 1], 6f);
                canvas.fill();
            }

            // ── UNIBUZZ header ─────────────────────────────────────────────────
            Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13f, new Color(165, 180, 252));
            Paragraph brand = new Paragraph("✦  U N I B U Z Z  ✦", brandFont);
            brand.setAlignment(Element.ALIGN_CENTER);
            brand.setSpacingBefore(40f);
            doc.add(brand);

            // ── Title ─────────────────────────────────────────────────────────
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 40f, new Color(255, 255, 255));
            Paragraph title = new Paragraph("Certificate of Participation", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(14f);
            doc.add(title);

            // ── Divider line ──────────────────────────────────────────────────
            addDivider(doc, new Color(99, 102, 241));

            // ── Body text ─────────────────────────────────────────────────────
            Font grayFont   = FontFactory.getFont(FontFactory.HELVETICA, 14f, new Color(148, 163, 184));
            Font nameFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 30f, new Color(165, 180, 252));
            Font eventFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 22f, new Color(255, 255, 255));
            Font smallFont  = FontFactory.getFont(FontFactory.HELVETICA, 12f, new Color(148, 163, 184));

            addCentered(doc, "This is to certify that", grayFont, 18f, 4f);
            addCentered(doc, studentName, nameFont, 4f, 6f);
            addCentered(doc, "has successfully participated in", grayFont, 6f, 4f);
            addCentered(doc, eventTitle, eventFont, 4f, 6f);

            // ── Divider line ──────────────────────────────────────────────────
            addDivider(doc, new Color(99, 102, 241));

            addCentered(doc, "Date: " + eventDate, smallFont, 10f, 0f);
            addCentered(doc, "Issued by UniBuzz · University Event Management Platform", smallFont, 4f, 0f);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF certificate", e);
        }
    }

    private void addCentered(Document doc, String text, Font font, float spacingBefore, float spacingAfter) throws DocumentException {
        Paragraph p = new Paragraph(text, font);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingBefore(spacingBefore);
        p.setSpacingAfter(spacingAfter);
        doc.add(p);
    }

    private void addDivider(Document doc, Color color) throws DocumentException {
        // Draw a colored line using a table with a colored border
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(70f);
        line.setSpacingBefore(12f);
        line.setSpacingAfter(12f);
        PdfPCell cell = new PdfPCell();
        cell.setBorderColorBottom(color);
        cell.setBorderWidthBottom(1.5f);
        cell.setBorderWidthTop(0);
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthRight(0);
        cell.setFixedHeight(1f);
        cell.setBackgroundColor(new Color(30, 27, 75)); // match bg
        line.addCell(cell);
        doc.add(line);
    }
}
