package com.aren.orderserver.services.impl;

import com.aren.orderserver.entities.User;
import com.aren.orderserver.exceptions.ResourceNotFoundException;
import com.aren.orderserver.repositories.UserRepository;
import com.aren.orderserver.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * Adds a new user to the system.
     *
     * @param user The user to be added
     * @return The added user
     * @throws DataIntegrityViolationException if a user with the same ID already exists
     */
    @Override
    @Transactional
    public User addUser(User user) {
        if (user.getId() != null && userRepository.existsById(user.getId())) {
            throw new DataIntegrityViolationException("User with id " + user.getId() + " already exists");
        }
        return userRepository.save(user);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id The ID of the user to retrieve
     * @return The user with the specified ID
     * @throws ResourceNotFoundException if the user is not found
     */
    @Override
    @Transactional(readOnly = true)
    public User getUser(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username The username of the user to retrieve
     * @return The user with the specified username
     * @throws ResourceNotFoundException if the user is not found
     */
    @Override
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        List<User> users = userRepository.findAll();
        return users.stream()
                .filter(it -> it.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User not found")) ;
    }

}
