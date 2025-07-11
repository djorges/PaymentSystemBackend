package org.example.paymentsystem.model;

public record LoginRequestDto(
    String email,
    String password
) {
}
