package com.aren.orderserver.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.aren.orderserver.entities.Order;
import com.aren.orderserver.entities.User;
import com.aren.orderserver.services.UserService;
import com.aren.orderserver.web.dto.auth.JwtRequest;
import com.aren.orderserver.web.dto.auth.JwtResponse;
import com.aren.orderserver.web.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@ContextConfiguration(classes = {AuthServiceImpl.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class AuthServiceImplTest {

    @Autowired
    private AuthServiceImpl authServiceImpl;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserService userService;

    /**
     * Method under test: {@link AuthServiceImpl#login(JwtRequest)}
     */
    @Test
    void testLogin() {
        // Arrange
        JwtRequest loginRequest = new JwtRequest("username", "password");

        User user = new User();
        user.setId(1);
        user.setUsername("username");
        user.setPassword("password");
        user.setRole("POSTER");

        when(userService.getUserByUsername("username")).thenReturn(user);
        when(jwtTokenProvider.createAccessToken(anyInt(), eq("username"), eq("POSTER"))).thenReturn("mockAccessToken");
        when(jwtTokenProvider.createRefreshToken(anyInt(), eq("username"))).thenReturn("mockRefreshToken");

        // Act
        JwtResponse jwtResponse = authServiceImpl.login(loginRequest);

        // Assert
        assertEquals(1, jwtResponse.getId().intValue());
        assertEquals("username", jwtResponse.getUsername());
        assertEquals("mockAccessToken", jwtResponse.getAccessToken());
        assertEquals("mockRefreshToken", jwtResponse.getRefreshToken());

        // Verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).getUserByUsername("username");
        verify(jwtTokenProvider).createAccessToken(1, "username", "POSTER");
        verify(jwtTokenProvider).createRefreshToken(1, "username");
    }

    /**
     * Method under test: {@link AuthServiceImpl#login(JwtRequest)}
     */
    @Test
    void testLoginInvalidCredentials() {
        // Arrange
        JwtRequest loginRequest = new JwtRequest("username", "wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act and Assert
        assertThrows(BadCredentialsException.class, () -> authServiceImpl.login(loginRequest));

        // Verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(userService);
        verifyNoInteractions(jwtTokenProvider);
    }

    /**
     * Method under test: {@link AuthServiceImpl#refresh(String)}
     */
    @Test
    void testRefreshTokens() {
        // Arrange
        String refreshToken = "mockRefreshToken";
        JwtResponse expectedResponse = new JwtResponse();
        expectedResponse.setAccessToken("newAccessToken");

        when(jwtTokenProvider.refreshUserTokens(refreshToken)).thenReturn(expectedResponse);

        // Act
        JwtResponse actualResponse = authServiceImpl.refresh(refreshToken);

        // Assert
        assertEquals("newAccessToken", actualResponse.getAccessToken());

        // Verify interactions
        verify(jwtTokenProvider).refreshUserTokens(refreshToken);
        verifyNoMoreInteractions(authenticationManager, userService);
    }
}