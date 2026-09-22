package com.example.tracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(name = "verification_token")
    private String verificationToken;

    // The day the user signed up = the first tracked day of the calendar
    @Column(name = "signup_date", nullable = false)
    private LocalDate signupDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public User(String email, String verificationToken) {
        this.email = email;
        this.verificationToken = verificationToken;
        this.signupDate = LocalDate.now();
        this.createdAt = Instant.now();
    }
}
