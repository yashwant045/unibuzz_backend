package com.unibuzz.crm.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username:crackthecode46@gmail.com}")
    private String fromEmail;

    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            if (fromEmail != null && !fromEmail.isBlank()) {
                helper.setFrom(fromEmail, "Unibuzz Notifications");
            }
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            
            // Commenting out the actual send call during development if credentials aren't set
            // to avoid errors in logs. But we'll leave it in for the real implementation.
            javaMailSender.send(message);
            log.info("Email sent to: " + to + " with subject: " + subject);
        } catch (Exception e) {
            log.error("Failed to send email to " + to, e);
        }
    }

    public void sendNewEventNotification(String to, String eventTitle, String eventDate, String location) {
        String subject = "New Event: " + eventTitle;
        String body = "<h3>A new event has been scheduled!</h3>"
                + "<p><strong>Title:</strong> " + eventTitle + "</p>"
                + "<p><strong>Date:</strong> " + eventDate + "</p>"
                + "<p><strong>Location:</strong> " + location + "</p>"
                + "<p>Don't miss out, register now!</p>";
        sendHtmlEmail(to, subject, body);
    }

    public void sendEventUpdateNotification(String to, String eventTitle, String eventDate, String location, String description, String category) {
        String subject = "📢 Event Update Notice: " + eventTitle;
        String body = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 12px; background-color: #ffffff;\">"
                + "<div style=\"background: linear-gradient(135deg, #4f46e5, #7c3aed); padding: 20px; border-radius: 8px; text-align: center; color: #ffffff;\">"
                + "<h2 style=\"margin: 0; font-size: 22px;\">📢 Event Update Notification</h2>"
                + "<p style=\"margin: 5px 0 0 0; font-size: 14px; opacity: 0.9;\">An event you registered for has been updated by faculty.</p>"
                + "</div>"
                + "<div style=\"padding: 20px 0; font-size: 15px; color: #334155;\">"
                + "<h3 style=\"color: #1e293b; margin-top: 10px;\">" + eventTitle + "</h3>"
                + "<div style=\"background-color: #f8fafc; border-left: 4px solid #4f46e5; padding: 15px; border-radius: 6px; margin: 15px 0;\">"
                + "<p style=\"margin: 4px 0;\"><strong>📅 Date:</strong> " + eventDate + "</p>"
                + "<p style=\"margin: 4px 0;\"><strong>📍 Location:</strong> " + location + "</p>"
                + (category != null && !category.isBlank() ? "<p style=\"margin: 4px 0;\"><strong>🏷️ Category:</strong> " + category + "</p>" : "")
                + "</div>"
                + (description != null && !description.isBlank() ? "<p style=\"color: #64748b; font-size: 14px;\"><strong>Description:</strong> " + description + "</p>" : "")
                + "<p style=\"margin-top: 20px; font-size: 14px; color: #64748b;\">Please visit the <a href=\"http://localhost:5173/dashboard/upcoming\" style=\"color: #4f46e5; font-weight: bold;\">UniBuzz Portal</a> for details.</p>"
                + "</div>"
                + "</div>";
        sendHtmlEmail(to, subject, body);
    }

    public void sendEventCancellationNotification(String to, String eventTitle) {
        String subject = "⚠️ Event Cancelled: " + eventTitle;
        String body = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px; border: 1px solid #fee2e2; border-radius: 12px; background-color: #ffffff;\">"
                + "<div style=\"background: linear-gradient(135deg, #ef4444, #dc2626); padding: 20px; border-radius: 8px; text-align: center; color: #ffffff;\">"
                + "<h2 style=\"margin: 0; font-size: 22px;\">⚠️ Event Cancelled</h2>"
                + "</div>"
                + "<div style=\"padding: 20px 0; font-size: 15px; color: #334155;\">"
                + "<p>The event <strong>" + eventTitle + "</strong> that you registered for has been cancelled by faculty.</p>"
                + "<p style=\"color: #64748b; font-size: 14px;\">You can browse and register for other upcoming events on the UniBuzz portal.</p>"
                + "</div>"
                + "</div>";
        sendHtmlEmail(to, subject, body);
    }

    public void sendUpcomingEventReminder(String to, String eventTitle, String eventDate, String location) {
        String subject = "Reminder: Upcoming Event Tomorrow - " + eventTitle;
        String body = "<h3>Friendly Reminder!</h3>"
                + "<p>The event <strong>" + eventTitle + "</strong> is happening tomorrow.</p>"
                + "<p><strong>Date:</strong> " + eventDate + "</p>"
                + "<p><strong>Location:</strong> " + location + "</p>"
                + "<p>We look forward to seeing you there!</p>";
        sendHtmlEmail(to, subject, body);
    }

    public void sendVerificationOtp(String to, String otp) {
        String subject = "Verify Your Unibuzz Account";
        String body = "<div style=\"font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;\">"
                + "<h2 style=\"color: #4f46e5; text-align: center;\">Welcome to Unibuzz!</h2>"
                + "<p>Thank you for registering. Please use the following 6-digit OTP code to verify your email address:</p>"
                + "<div style=\"text-align: center; margin: 25px 0;\">"
                + "<span style=\"font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #4f46e5; background: #f3f4f6; padding: 12px 24px; border-radius: 8px; display: inline-block;\">" + otp + "</span>"
                + "</div>"
                + "<p style=\"color: #6b7280; font-size: 14px;\">This code is valid for 15 minutes. If you did not create an account on Unibuzz, please ignore this email.</p>"
                + "</div>";
        sendHtmlEmail(to, subject, body);
    }

    public void sendPasswordResetOtp(String to, String otp) {
        String subject = "Password Reset Request - Unibuzz";
        String body = "<div style=\"font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;\">"
                + "<h2 style=\"color: #e11d48; text-align: center;\">Password Reset Request</h2>"
                + "<p>We received a request to reset your Unibuzz account password. Use the following 6-digit OTP code to complete your password reset:</p>"
                + "<div style=\"text-align: center; margin: 25px 0;\">"
                + "<span style=\"font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #e11d48; background: #fff1f2; padding: 12px 24px; border-radius: 8px; display: inline-block;\">" + otp + "</span>"
                + "</div>"
                + "<p style=\"color: #6b7280; font-size: 14px;\">This code is valid for 15 minutes. If you did not request a password reset, please secure your account immediately.</p>"
                + "</div>";
        sendHtmlEmail(to, subject, body);
    }
}
