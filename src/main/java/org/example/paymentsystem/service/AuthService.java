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
/**
 * TODO: Create Docs
 * */
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public TokenResponseDto register(RegisterRequestDto request){
        var user = UserEntity.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        var savedUser = userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(savedUser, jwtToken);

        return new TokenResponseDto(jwtToken, refreshToken);
    }

    public TokenResponseDto login(LoginRequestDto request){
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    request.email(),
                    request.password()
            )
        );

        var user = userRepository.findByEmail(request.email()).orElseThrow(
            () -> new UsernameNotFoundException("User not found")
        );
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return new TokenResponseDto(jwtToken, refreshToken);
    }

    public TokenResponseDto refreshToken(final String authHeader){
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            throw new IllegalArgumentException("Invalid Bearer Token");
        }

        final String refreshToken = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(refreshToken);
        if(userEmail == null){
            throw new IllegalArgumentException("Invalid Refresh Token");
        }

        return null;
    }


    private void revokeAllUserTokens(UserEntity user) {
        final List<TokenEntity> validUserTokens = tokenRepository.findAllExpiredIsFalseOrRevokedIsFalseByUserId(user.getId());
        if (validUserTokens.isEmpty()) {
            return;
        }

        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    private void saveUserToken(UserEntity user, String jwtToken){
        var token = TokenEntity.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();

        tokenRepository.save(token);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("USER")
                .build();
    }
}
