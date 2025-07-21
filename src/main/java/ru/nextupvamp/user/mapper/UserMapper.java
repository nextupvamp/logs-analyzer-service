package ru.nextupvamp.user.mapper;

import org.springframework.stereotype.Component;
import ru.nextupvamp.user.domain.User;
import ru.nextupvamp.user.dto.UserDto;

@Component
public class UserMapper {

    public UserDto mapDomainToDto(User user) {
        return new UserDto(
                user.login(),
                user.password()
        );
    }
}
