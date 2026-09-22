package com.example.tracker.service;

import com.example.tracker.dto.CalendarResponse;
import com.example.tracker.entity.DayEntry;
import com.example.tracker.entity.DayStatus;
import com.example.tracker.entity.User;
import com.example.tracker.repository.DayEntryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DayEntryService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private final DayEntryRepository dayEntryRepository;

    public DayEntryService(DayEntryRepository dayEntryRepository) {
        this.dayEntryRepository = dayEntryRepository;
    }

    public DayEntry setStatus(User user, LocalDate date, DayStatus status) {
        DayEntry entry = dayEntryRepository.findByUserAndDate(user, date)
                .orElse(new DayEntry(user, date, status));
        entry.setStatus(status);
        return dayEntryRepository.save(entry);
    }

    /**
     * Full calendar payload: every recorded entry since signup, plus overall counts.
     */
    public CalendarResponse buildCalendar(User user) {
        List<DayEntry> all = dayEntryRepository.findByUser(user);

        Map<String, String> entries = new HashMap<>();
        long done = 0, tried = 0, notDone = 0;
        for (DayEntry entry : all) {
            entries.put(entry.getDate().format(ISO), entry.getStatus().name());
            switch (entry.getStatus()) {
                case DONE -> done++;
                case TRIED -> tried++;
                case NOT_DONE -> notDone++;
            }
        }

        return new CalendarResponse(user.getSignupDate(), entries,
                new CalendarResponse.Stats(done, tried, notDone));
    }
}
