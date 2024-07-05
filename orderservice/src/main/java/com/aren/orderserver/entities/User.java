package com.aren.orderserver.entities;

import com.aren.orderserver.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password; //todo hashed jwt

    @Column(name = "email")
    private String email;

    @Column(name = "role")
    private String role;

    public User(String username,
                String password,
                String email,
                String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

}
