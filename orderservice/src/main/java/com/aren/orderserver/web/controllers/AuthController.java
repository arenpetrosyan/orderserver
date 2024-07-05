package com.aren.orderserver.web.controllers;

import com.aren.orderserver.entities.User;
import com.aren.orderserver.services.AuthService;
import com.aren.orderserver.services.UserService;
import com.aren.orderserver.web.validation.OnCreate;
import com.aren.orderserver.web.dto.UserDto;
import com.aren.orderserver.web.dto.auth.JwtRequest;
import com.aren.orderserver.web.dto.auth.JwtResponse;
import com.aren.orderserver.web.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    private final UserMapper userMapper;

    /**
     * Endpoint for user login using JWT authentication.
     *
     * @param loginRequest The JwtRequest object containing user login credentials
     * @return JwtResponse containing user ID, username, access token, and refresh token
     */
    @PostMapping("/login")
    public JwtResponse login(@Validated @RequestBody final JwtRequest loginRequest) {
        return authService.login(loginRequest);
    }

    /**
     * Endpoint for user registration.
     *
     * @param userDto The UserDto object containing user registration details
     * @return UserDto representing the registered user
     */
    @PostMapping("/register")
    public UserDto register(@Validated(OnCreate.class) @RequestBody final UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        User createdUser = userService.addUser(user);
        return userMapper.toDto(createdUser);
    }

    /**
     * Endpoint to refresh the user's access token using the provided refresh token.
     *
     * @param refreshToken The refresh token string
     * @return JwtResponse containing the new access token
     */
    @PostMapping("/refresh")
    public JwtResponse refresh(@RequestBody String refreshToken) {
        return authService.refresh(refreshToken);
    }

}
