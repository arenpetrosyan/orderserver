package com.aren.orderserver.services;

import com.aren.orderserver.web.dto.auth.JwtRequest;
import com.aren.orderserver.web.dto.auth.JwtResponse;

public interface AuthService {

    JwtResponse login(JwtRequest login);
    JwtResponse refresh(String refreshToken);

}
