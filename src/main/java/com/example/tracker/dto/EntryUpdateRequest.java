package com.example.tracker.dto;

import com.example.tracker.entity.DayStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class EntryUpdateRequest {

    @NotBlank
    @Email
    private String email;

    @NotNull
    private LocalDate date;

    @NotNull
    private DayStatus status;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public DayStatus getStatus() { return status; }
    public void setStatus(DayStatus status) { this.status = status; }
}
