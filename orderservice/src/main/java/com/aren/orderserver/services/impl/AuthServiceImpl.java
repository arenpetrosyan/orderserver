package com.aren.orderserver.services.impl;

import com.aren.orderserver.entities.User;
import com.aren.orderserver.services.AuthService;
import com.aren.orderserver.services.UserService;
import com.aren.orderserver.web.dto.auth.JwtRequest;
import com.aren.orderserver.web.dto.auth.JwtResponse;
import com.aren.orderserver.web.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Logs in the User by authenticating the provided credentials and generates JWT tokens for the user.
     *
     * @param loginRequest The login request containing the username and password
     * @return JwtResponse containing the user ID, username, access token, and refresh token
     */
    @Override
    public JwtResponse login(JwtRequest loginRequest) {
        JwtResponse jwtResponse = new JwtResponse();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        User user = userService.getUserByUsername(loginRequest.getUsername());

        jwtResponse.setId(user.getId());
        jwtResponse.setUsername(user.getUsername());
        jwtResponse.setAccessToken(jwtTokenProvider.createAccessToken(user.getId(), user.getUsername(), user.getRole()));
        jwtResponse.setRefreshToken(jwtTokenProvider.createRefreshToken(user.getId(), user.getUsername()));
        return jwtResponse;
    }

    /**
     * Refreshes the user's access token using the provided refresh token.
     *
     * @param refreshToken The refresh token to generate a new access token
     * @return JwtResponse containing the new access token
     */
    @Override
    public JwtResponse refresh(String refreshToken) {
        return jwtTokenProvider.refreshUserTokens(refreshToken);
    }
}
