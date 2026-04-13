package com.kickoff.user_service.controller;

import com.kickoff.user_service.dto.AuthResponse;
import com.kickoff.user_service.dto.LoginRequest;
import com.kickoff.user_service.dto.RegisterRequest;
import com.kickoff.user_service.dto.UserResponse;
import com.kickoff.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User registration and authentication")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT token")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user profile")
    public UserResponse getUser(@PathVariable UUID userId) {
        return userService.getUser(userId);
    }
}