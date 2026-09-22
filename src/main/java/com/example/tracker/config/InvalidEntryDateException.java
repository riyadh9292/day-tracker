package com.example.tracker.config;

public class InvalidEntryDateException extends RuntimeException {
    public InvalidEntryDateException(String message) {
        super(message);
    }
}