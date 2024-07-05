package com.aren.orderserver.services.impl;

import com.aren.orderserver.entities.User;
import com.aren.orderserver.exceptions.ResourceNotFoundException;
import com.aren.orderserver.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {UserServiceImpl.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class UserServiceImplTest {

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userServiceImpl;

    /**
     * Method under test: {@link UserServiceImpl#addUser(User)}
     */
    @Test
    void testAddUser() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("username");
        newUser.setId(1);

        when(userRepository.existsById(anyInt())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(newUser);

        // Act
        User addedUser = userServiceImpl.addUser(newUser);

        // Assert
        verify(userRepository).existsById(1);
        verify(userRepository).save(newUser);
        assertSame(newUser, addedUser);
    }


    /**
     * Method under test: {@link UserServiceImpl#addUser(User)}
     */
    @Test
    void testAddUserExistsThrowsResourceNotFound() {
        // Arrange
        when(userRepository.existsById(anyInt())).thenThrow(new ResourceNotFoundException("An error occurred"));

        User user = new User();
        user.setId(1);
        user.setUsername("username");

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> userServiceImpl.addUser(user));
        verify(userRepository).existsById(eq(1));
    }

    /**
     * Method under test: {@link UserServiceImpl#addUser(User)}
     */
    @Test
    void testAUserSaveThrowsResourceNotFound() {
        // Arrange
        when(userRepository.save(any())).thenThrow(new ResourceNotFoundException("An error occurred"));

        User user = new User();
        user.setUsername("username");

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> userServiceImpl.addUser(user));
        verify(userRepository).save(any(User.class));
    }

    /**
     * Method under test: {@link UserServiceImpl#getUser(Integer)}
     */
    @Test
    void testGetUser() {
        // Arrange
        User user = new User();
        user.setId(1);
        user.setUsername("username");

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));

        // Act
        User retrievedUser = userServiceImpl.getUser(1);

        // Assert
        verify(userRepository).findById(eq(1));
        assertSame(user, retrievedUser);
    }

    /**
     * Method under test: {@link UserServiceImpl#getUser(Integer)}
     */
    @Test
    void testGetUserResourceNotFound() {
        // Arrange
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> userServiceImpl.getUser(1));
        verify(userRepository).findById(eq(1));
    }

    /**
     * Method under test: {@link UserServiceImpl#getUser(Integer)}
     */
    @Test
    void testGetUserThrowsDataIntegrityViolation() {
        // Arrange
        when(userRepository.findById(anyInt()))
                .thenThrow(new DataIntegrityViolationException("User not found"));

        // Act and Assert
        assertThrows(DataIntegrityViolationException.class, () -> userServiceImpl.getUser(1));
        verify(userRepository).findById(eq(1));
    }

    /**
     * Method under test: {@link UserServiceImpl#getUserByUsername(String)}
     */
    @Test
    void testGetUserByUsernameThrowsResourceNotFound() {
        // Arrange
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> userServiceImpl.getUserByUsername("username"));
        verify(userRepository).findAll();
    }

    /**
     * Method under test: {@link UserServiceImpl#getUserByUsername(String)}
     */
    @Test
    void testGetUserByUsernameUserWithUsernameExists() {
        // Arrange
        User user = new User();
        user.setId(1);
        user.setUsername("username");

        ArrayList<User> userList = new ArrayList<>();
        userList.add(user);
        when(userRepository.findAll()).thenReturn(userList);

        // Act
        User retrievedUser = userServiceImpl.getUserByUsername("username");

        // Assert
        verify(userRepository).findAll();
        assertSame(user, retrievedUser);
    }

}
