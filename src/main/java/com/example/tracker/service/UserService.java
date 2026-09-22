package com.example.tracker.service;

import com.example.tracker.entity.User;
import com.example.tracker.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /**
     * Sign up (or re-request verification for) an email address.
     * If the email already exists and is verified, we just resend nothing —
     * caller treats this as "you're already good to go".
     */
    public User signup(String email) {
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            User user = existing.get();
            if (!user.isVerified()) {
                // re-issue a token and resend the email
                user.setVerificationToken(UUID.randomUUID().toString());
                userRepository.save(user);
                emailService.sendVerificationEmail(email, user.getVerificationToken());
            }
            return user;
        }

        User user = new User(email, UUID.randomUUID().toString());
        userRepository.save(user);
        emailService.sendVerificationEmail(email, user.getVerificationToken());
        return user;
    }

    public Optional<User> verify(String token) {
        Optional<User> found = userRepository.findByVerificationToken(token);
        found.ifPresent(user -> {
            user.setVerified(true);
            user.setVerificationToken(null);
            userRepository.save(user);
        });
        return found;
    }

    public Optional<User> findVerifiedUser(String email) {
        return userRepository.findByEmail(email).filter(User::isVerified);
    }
}
