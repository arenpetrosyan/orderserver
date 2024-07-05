package com.aren.orderserver.web.security;

import com.aren.orderserver.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JwtEntityFactory {

    /**
     * Creates a JwtEntity from a given User object.
     *
     * @param user the User object
     * @return a JwtEntity containing the user's details and authorities
     */
    public static JwtEntity create(
            final User user) {
        return new JwtEntity(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                mapToGrantedAuthorities(user.getRole())
        );
    }

    /**
     * Maps a role to a list of GrantedAuthority objects.
     *
     * @param role the role of the user
     * @return a list of GrantedAuthority objects containing the user's role
     */
    private static List<GrantedAuthority> mapToGrantedAuthorities(final String role) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
        return new ArrayList<>(Collections.singleton(authority));
    }
}
