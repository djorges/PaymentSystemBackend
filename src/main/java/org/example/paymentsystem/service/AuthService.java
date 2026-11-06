package org.example.paymentsystem.service;

import lombok.RequiredArgsConstructor;
import org.example.paymentsystem.entity.TokenEntity;
import org.example.paymentsystem.entity.UserEntity;
import org.example.paymentsystem.entity.enums.TokenType;
import org.example.paymentsystem.model.LoginRequestDto;
import org.example.paymentsystem.model.RegisterRequestDto;
import org.example.paymentsystem.model.TokenResponseDto;
import org.example.paymentsystem.repository.TokenRepository;
import org.example.paymentsystem.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Project: PaymentSystem
 * Author: djorges
 * Date: 03/07/2025
 */


@Service
@RequiredArgsConstructor
@Transactional
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public TokenResponseDto register(RegisterRequestDto request) {

        if (emailExists(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        var user = UserEntity.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        var savedUser = userRepository.save(user);

        return generateTokens(savedUser);
    }

    public TokenResponseDto login(LoginRequestDto request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        var user = getUserByEmail(request.email());

        revokeAllUserTokens(user);

        return generateTokens(user);
    }

    // 1. Renovar tokens
    public TokenResponseDto refreshToken(HttpServletRequest request) {

        String token = extractBearerToken(request);

        if (token == null) {
            throw new IllegalArgumentException("Refresh token is missing");
        }

        var storedToken = tokenRepository.findByToken(token)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid refresh token"));

        if (storedToken.isRevoked() || storedToken.isExpired()) {
            throw new IllegalArgumentException("Refresh token revoked or expired");
        }

        String username = jwtService.extractUsername(token);

        var user = getUserByEmail(username);

        if (!jwtService.isRefreshTokenValid(token, user)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        revokeAllUserTokens(user);

        return generateTokens(user);
    }

    // 2. Cerrar sesión actual
    public void logout(HttpServletRequest request) {

        String token = extractBearerToken(request);

        if (token == null) {
            return;
        }

        tokenRepository.findByToken(token).ifPresent(storedToken -> {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
        });
    }

    // 3. Cerrar todas las sesiones
    public void logoutAll(Long userId) {

        var user = getUserById(userId);

        revokeAllUserTokens(user);
    }

    // 4. Cambiar contraseña
    public void changePassword(
            Long userId,
            String currentPassword,
            String newPassword
    ) {

        var user = getUserById(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect current password");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different");
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        revokeAllUserTokens(user);
    }

    // 5. Buscar usuario por email
    @Transactional(readOnly = true)
    public UserEntity getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));
    }

    // 6. Buscar usuario por ID
    @Transactional(readOnly = true)
    public UserEntity getUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));
    }

    // 7. Comprobar si existe un email
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {

        return userRepository.findByEmail(email).isPresent();
    }

    // 8. Validar contraseña
    @Transactional(readOnly = true)
    public boolean isPasswordValid(String email, String password) {

        var user = getUserByEmail(email);

        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {

        return getUserByEmail(username);
    }

    private TokenResponseDto generateTokens(UserEntity user) {

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(user, accessToken);
        saveUserToken(user, refreshToken);

        return new TokenResponseDto(accessToken, refreshToken);
    }

    private void saveUserToken(UserEntity user, String token) {

        var tokenEntity = TokenEntity.builder()
                .user(user)
                .token(token)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();

        tokenRepository.save(tokenEntity);
    }

    private void revokeAllUserTokens(UserEntity user) {

        var validTokens = tokenRepository
                .findAllByUser_IdAndExpiredFalseAndRevokedFalse(user.getId());

        if (validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });

        tokenRepository.saveAll(validTokens);
    }

    private String extractBearerToken(HttpServletRequest request) {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        return authorizationHeader.substring(7);
    }
}
