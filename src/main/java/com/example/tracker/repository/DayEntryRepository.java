package com.example.tracker.repository;

import com.example.tracker.entity.DayEntry;
import com.example.tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DayEntryRepository extends JpaRepository<DayEntry, Long> {

    Optional<DayEntry> findByUserAndDate(User user, LocalDate date);

    List<DayEntry> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);

    List<DayEntry> findByUser(User user);
}
