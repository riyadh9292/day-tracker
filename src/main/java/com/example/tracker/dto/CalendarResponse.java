package com.example.tracker.dto;

import java.time.LocalDate;
import java.util.Map;

public class CalendarResponse {

    private LocalDate signupDate;
    private Map<String, String> entries; // "yyyy-MM-dd" -> DONE/TRIED/NOT_DONE
    private Stats stats;

    public CalendarResponse(LocalDate signupDate, Map<String, String> entries, Stats stats) {
        this.signupDate = signupDate;
        this.entries = entries;
        this.stats = stats;
    }

    public LocalDate getSignupDate() { return signupDate; }
    public Map<String, String> getEntries() { return entries; }
    public Stats getStats() { return stats; }

    public static class Stats {
        private long done;
        private long tried;
        private long notDone;

        public Stats(long done, long tried, long notDone) {
            this.done = done;
            this.tried = tried;
            this.notDone = notDone;
        }

        public long getDone() { return done; }
        public long getTried() { return tried; }
        public long getNotDone() { return notDone; }
    }
}
