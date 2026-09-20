package com.unibuzz.crm.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name="users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;
    private String password;

    @Builder.Default
    private boolean emailVerified = false;
    private String verificationOtp;
    private LocalDateTime otpExpiry;

    private String enrollmentNumber;
    private String phoneNumber;
    private String department;
    private String section;
    private String year;

    private String designation;
    private String expertise;
    private String officeLocation;

    @ManyToMany
    @JoinTable(
            name = "user_interests",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_id")
    )
    private Set<Interest> interests;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;

    public String getRole() {
        return roles.stream()
                .findFirst()
                .map(Role::getName)
                .orElse("");
    }
}
