package ru.nextupvamp.user.service;

import ru.nextupvamp.resource.dto.ResourceDto;
import ru.nextupvamp.user.dto.LoginResponse;
import ru.nextupvamp.user.dto.UserDto;

import java.util.List;

public interface UserService {

    LoginResponse addNewUser(UserDto userData);

    UserDto getUserByLogin(String login);

    void deleteUser(String login);

    List<UserDto> getAllUsers();

    List<ResourceDto> getUserResources(String login);
}
