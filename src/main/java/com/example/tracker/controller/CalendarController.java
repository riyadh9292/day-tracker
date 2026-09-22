package com.example.tracker.controller;

import com.example.tracker.config.InvalidEntryDateException;
import com.example.tracker.dto.CalendarResponse;
import com.example.tracker.dto.EntryUpdateRequest;
import com.example.tracker.entity.User;
import com.example.tracker.service.DayEntryService;
import com.example.tracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final UserService userService;
    private final DayEntryService dayEntryService;

    public CalendarController(UserService userService, DayEntryService dayEntryService) {
        this.userService = userService;
        this.dayEntryService = dayEntryService;
    }

    @GetMapping
    public ResponseEntity<CalendarResponse> getCalendar(@RequestParam String email) {
        User user = userService.findVerifiedUser(email)
                .orElseThrow(() -> new IllegalStateException("Email not verified"));
        return ResponseEntity.ok(dayEntryService.buildCalendar(user));
    }

    @PutMapping("/entry")
    public ResponseEntity<CalendarResponse> updateEntry(@Valid @RequestBody EntryUpdateRequest request) {
        if (!request.getDate().isEqual(LocalDate.now())) {
            throw new InvalidEntryDateException("Only today's entry can be updated. Received: " + request.getDate());
        }
        User user = userService.findVerifiedUser(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("Email not verified"));
        dayEntryService.setStatus(user, request.getDate(), request.getStatus());
        return ResponseEntity.ok(dayEntryService.buildCalendar(user));
    }
}
