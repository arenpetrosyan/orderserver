package com.aren.orderserver.web.mappers;

import com.aren.orderserver.entities.User;
import com.aren.orderserver.web.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper extends Mappable<User, UserDto> {

}
