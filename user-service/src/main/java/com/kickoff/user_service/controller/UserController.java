package com.kickoff.user_service.controller;

import com.kickoff.user_service.dto.AuthResponse;
import com.kickoff.user_service.dto.LoginRequest;
import com.kickoff.user_service.dto.RegisterRequest;
import com.kickoff.user_service.dto.UserResponse;
import com.kickoff.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User registration, authentication and profile")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new account and returns a JWT token immediately — no separate login required. Username must be 3-50 characters, password minimum 6 characters")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates with username and password. Returns a JWT token valid for 24 hours. Use the token in Authorization: Bearer <token> header for all protected endpoints")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user profile", description = "Returns username, email and registration date for the specified user")
    public UserResponse getUser(@PathVariable UUID userId) {
        return userService.getUser(userId);
    }
}