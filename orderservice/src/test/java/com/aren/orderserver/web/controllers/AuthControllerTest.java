package com.aren.orderserver.web.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.aren.orderserver.services.AuthService;
import com.aren.orderserver.web.dto.UserDto;
import com.aren.orderserver.web.dto.auth.JwtRequest;
import com.aren.orderserver.web.dto.auth.JwtResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ContextConfiguration(classes = {AuthController.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class AuthControllerTest {

    @Autowired
    private AuthController authController;

    @MockBean
    private AuthService authService;


    /**
     * Method under test: {@link AuthController#login(JwtRequest)}
     */
    @Test
    void testLogin() throws Exception {
        // Arrange
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setAccessToken("ABC123");
        jwtResponse.setId(1);
        jwtResponse.setRefreshToken("ABC123");
        jwtResponse.setUsername("username");
        when(authService.login(any())).thenReturn(jwtResponse);

        JwtRequest jwtRequest = new JwtRequest();
        jwtRequest.setPassword("password");
        jwtRequest.setUsername("username");
        String content = (new ObjectMapper()).writeValueAsString(jwtRequest);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);

        // Act and Assert
        MockMvcBuilders.standaloneSetup(authController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content()
                        .string("{\"id\":1,\"username\":\"username\",\"accessToken\":\"ABC123\",\"refreshToken\":\"ABC123\"}"));
    }

    /**
     * Method under test: {@link AuthController#refresh(String)}
     */
    @Test
    void testRefresh() throws Exception {
        // Arrange
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setAccessToken("ABC123");
        jwtResponse.setId(1);
        jwtResponse.setRefreshToken("ABC123");
        jwtResponse.setUsername("username");
        when(authService.refresh(any())).thenReturn(jwtResponse);
        MockHttpServletRequestBuilder contentTypeResult = MockMvcRequestBuilders.post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON);
        MockHttpServletRequestBuilder requestBuilder = contentTypeResult
                .content((new ObjectMapper()).writeValueAsString("test"));

        // Act and Assert
        MockMvcBuilders.standaloneSetup(authController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content()
                        .string("{\"id\":1,\"username\":\"username\",\"accessToken\":\"ABC123\",\"refreshToken\":\"ABC123\"}"));
    }

    /**
     * Method under test: {@link AuthController#register(UserDto)}
     */
    @Test
    void testRegister() throws Exception {
        // Arrange
        UserDto userDto = new UserDto();
        userDto.setEmail("test@mail.com");
        userDto.setId(1);
        userDto.setPassword("password");
        userDto.setRole("Role");
        userDto.setUsername("username");
        String content = (new ObjectMapper()).writeValueAsString(userDto);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);

        // Act
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(authController).build().perform(requestBuilder);

        // Assert
        actualPerformResult.andExpect(MockMvcResultMatchers.status().is(400));
    }

}
