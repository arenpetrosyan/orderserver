package com.aren.orderserver.services;

import com.aren.orderserver.entities.User;

public interface UserService {
    User addUser(User user);

    User getUser(Integer id);

    User getUserByUsername(String username);
}
