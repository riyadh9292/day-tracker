package com.example.tracker.controller;

import com.example.tracker.dto.SignupRequest;
import com.example.tracker.entity.User;
import com.example.tracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody SignupRequest request) {
        User user = userService.signup(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "email", user.getEmail(),
                "verified", user.isVerified(),
                "message", user.isVerified()
                        ? "Already verified — go ahead and open your calendar."
                        : "Check your inbox for a verification link."
        ));
    }

    // Verification link lands here; redirect back to the app with a status flag.
    @GetMapping("/verify")
    public ResponseEntity<Void> verify(@RequestParam String token) {
        Optional<User> user = userService.verify(token);
        String redirect = user.isPresent() ? "/?verified=true" : "/?verified=false";
        return ResponseEntity.status(302).header("Location", redirect).build();
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(@RequestParam String email) {
        Optional<User> user = userService.findVerifiedUser(email);
        return ResponseEntity.ok(Map.of("verified", user.isPresent()));
    }
}
