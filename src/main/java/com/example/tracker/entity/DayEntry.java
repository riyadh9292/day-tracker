package com.example.tracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "day_entries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "entry_date"}))
@Getter
@Setter
@NoArgsConstructor
public class DayEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "entry_date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayStatus status;

    public DayEntry(User user, LocalDate date, DayStatus status) {
        this.user = user;
        this.date = date;
        this.status = status;
    }
}
