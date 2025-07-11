package org.example.paymentsystem.model;

/**
 * Project: PaymentSystem
 * Author: djorges
 * Date: 04/07/2025
 */


public record TokenResponseDto(
    String token,
    String refreshToken
) {
}
