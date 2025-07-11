package org.example.paymentsystem.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.lang.Maps;
import io.jsonwebtoken.security.Keys;
import lombok.val;
import org.example.paymentsystem.entity.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Project: PaymentSystem
 * Author: djorges
 * Date: 04/07/2025
 */


@Service
public class JwtService {
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    /**
     * Genera un JWT (token de acceso) para un usuario autenticado.
     *
     * @param user el usuario para el que se generará el token
     * @return una cadena JWT firmada que expira según la configuración de `jwtExpiration`
     */
    public String generateToken(final UserEntity user) {
        return buildToken(user, jwtExpiration);
    }

    /**
     * Genera un JWT de tipo "refresh token" para un usuario, usado para renovar el token de acceso.
     *
     * @param user el usuario para el que se generará el token de renovación
     * @return una cadena JWT firmada que expira según la configuración de `refreshExpiration`
     */
    public String generateRefreshToken(final UserEntity user) {
        return buildToken(user, refreshExpiration);
    }

    /**
     * Construye y firma un JWT con los datos del usuario y una duración específica.
     *
     * @param user el usuario cuyos datos se incluirán en el token
     * @param expiration el tiempo de expiración del token en milisegundos
     * @return una cadena JWT firmada
     */
    private String buildToken(final UserEntity user, final long expiration) {
        return Jwts.builder()
                .setId(user.getId().toString())
                .setClaims((Claims) Maps.of("name", user.getName()))
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Decodifica la clave secreta base64 y genera una clave HMAC-SHA válida para firmar los tokens JWT.
     *
     * @return una clave secreta HMAC lista para firmar los JWT
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String refreshToken) {
        val jwtToken = Jwts.
    }
}
