package org.example.paymentsystem.model;

public record RegisterRequestDto(
    String email,
    String password,
    String name
) {
}
