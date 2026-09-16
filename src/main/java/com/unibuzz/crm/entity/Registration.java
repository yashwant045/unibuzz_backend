package com.unibuzz.crm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "registrations",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"student_email", "event_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_email", nullable = false)
    private String studentEmail;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    /** Set to true by faculty/admin after the student physically attends the event. */
    @Builder.Default
    @Column(nullable = false)
    private boolean attended = false;
}
