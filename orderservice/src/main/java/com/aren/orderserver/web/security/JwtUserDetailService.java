package com.aren.orderserver.web.security;

import com.aren.orderserver.entities.User;
import com.aren.orderserver.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtUserDetailService implements UserDetailsService {

    private final UserService userService;

    /**
     * Loads user details by username.
     *
     * @param username the username to load details for
     * @return UserDetails object for the specified user
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userService.getUserByUsername(username);
        return JwtEntityFactory.create(user);
    }
}
