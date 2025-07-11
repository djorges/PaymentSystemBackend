package org.example.paymentsystem.controller;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.example.paymentsystem.model.LoginRequestDto;
import org.example.paymentsystem.model.RegisterRequestDto;
import org.example.paymentsystem.model.TokenResponseDto;
import org.example.paymentsystem.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Project: PaymentSystem
 * Author: djorges
 * Date: 01/07/2025
 */

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<TokenResponseDto> register(
        @RequestBody final RegisterRequestDto request
    ){
        TokenResponseDto token = service.register(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(
        @RequestBody final LoginRequestDto request
    ){
        TokenResponseDto token = service.login(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/refresh")
    public TokenResponseDto refreshToken(
        @RequestHeader(HttpHeaders.AUTHORIZATION) final String authHeader
    ){
        return service.refreshToken(authHeader);
    }
}
