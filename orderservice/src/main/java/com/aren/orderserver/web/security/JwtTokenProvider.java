package com.aren.orderserver.web.security;

import com.aren.orderserver.entities.User;
import com.aren.orderserver.exceptions.AccessDeniedException;
import com.aren.orderserver.services.UserService;
import com.aren.orderserver.web.dto.auth.JwtResponse;
import com.aren.orderserver.web.security.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private SecretKey key;

    /**
     * Initializes the JWT key after construction.
     */
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    /**
     * Creates an access token for the given user details.
     *
     * @param userId   the user ID
     * @param username the username
     * @param role     the role of the user
     * @return the generated JWT access token
     */
    public String createAccessToken(int userId, String username, String role) {
        Claims claims = Jwts.claims()
                .subject(username)
                .add("id", userId)
                .add("roles", role)
                .build();
        Instant validity = Instant.now()
                .plus(Long.parseLong(jwtProperties.getAccess()), ChronoUnit.HOURS);
        return Jwts.builder()
                .claims(claims)
                .expiration(Date.from(validity))
                .signWith(key)
                .compact();
    }

    /**
     * Creates a refresh token for the given user details.
     *
     * @param userId   the user ID
     * @param username the username
     * @return the generated JWT refresh token
     */
    public String createRefreshToken(int userId, String username) {
        Claims claims = Jwts.claims()
                .subject(username)
                .add("id", userId)
                .build();
        Instant validity = Instant.now()
                .plus(Long.parseLong(jwtProperties.getRefresh()), ChronoUnit.DAYS);
        return Jwts.builder()
                .claims(claims)
                .expiration(Date.from(validity))
                .signWith(key)
                .compact();
    }

    /**
     * Refreshes the user tokens if the provided refresh token is valid.
     *
     * @param refreshToken the refresh token
     * @return the new JWT access and refresh tokens
     */
    public JwtResponse refreshUserTokens(final String refreshToken) {
        JwtResponse jwtResponse = new JwtResponse();
        if (!isValid(refreshToken)) {
            throw new AccessDeniedException("Access denied");
        }
        int userId = Integer.parseInt(getId(refreshToken));
        User user = userService.getUser(userId);
        jwtResponse.setId(userId);
        jwtResponse.setUsername(user.getUsername());
        jwtResponse.setAccessToken(createAccessToken(userId, user.getUsername(), user.getRole()));
        jwtResponse.setRefreshToken(createRefreshToken(userId, user.getUsername()));
        return jwtResponse;
    }

    /**
     * Retrieves the authentication object from the provided JWT token.
     *
     * @param token the JWT token
     * @return the authentication object
     */
    public Authentication getAuthentication(final String token) {
        String username = getUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                "",
                userDetails.getAuthorities()
        );
    }

    /**
     * Validates the provided JWT token.
     *
     * @param token the JWT token
     * @return true if the token is valid, false otherwise
     */
    public boolean isValid(final String token) {
        Jws<Claims> claims = Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
        return claims.getPayload()
                .getExpiration()
                .after(new Date());
    }

    /**
     * Retrieves the username from the provided JWT token.
     *
     * @param token the JWT token
     * @return the username
     */
    private String getUsername(final String token) {
        return Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Retrieves the user ID from the provided JWT token.
     *
     * @param token the JWT token
     * @return the user ID
     */
    private String getId(final String token) {
        return Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("id", String.class);
    }

}
