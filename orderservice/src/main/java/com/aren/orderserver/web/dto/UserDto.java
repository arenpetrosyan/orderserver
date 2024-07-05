package com.aren.orderserver.web.dto;

import com.aren.orderserver.annotations.CheckEmail;
import com.aren.orderserver.web.validation.OnCreate;
import com.aren.orderserver.web.validation.OnUpdate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserDto {

    @NotNull(message = "Id must not be null", groups = OnUpdate.class)
    private Integer id;

    @NotNull(message = "Name must not be null", groups = {OnUpdate.class, OnCreate.class})
    @Length(max = 255, message = "Name must not be bigger than 255 symbols", groups = {OnUpdate.class, OnCreate.class})
    private String username;

    @NotNull(message = "Password must not be null", groups = {OnUpdate.class, OnCreate.class})
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotNull(message = "Email must not be null", groups = {OnUpdate.class, OnCreate.class})
    @CheckEmail
    private String email;

    @NotNull
    private String role;

}
