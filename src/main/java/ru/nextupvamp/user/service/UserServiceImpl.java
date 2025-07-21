package ru.nextupvamp.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nextupvamp.resource.dto.ResourceDto;
import ru.nextupvamp.resource.mapper.ResourceMapper;
import ru.nextupvamp.user.domain.User;
import ru.nextupvamp.user.dto.LoginResponse;
import ru.nextupvamp.user.dto.UserDto;
import ru.nextupvamp.user.mapper.UserMapper;
import ru.nextupvamp.user.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Supplier<NoSuchElementException> USER_NOT_FOUND =
            () -> new NoSuchElementException("User Not Found");

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ResourceMapper resourceMapper;

    @Override
    public LoginResponse addNewUser(UserDto userData) {
        User user = new User();
        user.login(userData.login());
        user.password(userData.password());
        if (userRepository.findById(user.login()).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        return new LoginResponse(userRepository.save(user).login());
    }

    @Override
    public UserDto getUserByLogin(String login) {
        return userRepository.findById(login)
                .map(userMapper::mapDomainToDto)
                .orElseThrow(USER_NOT_FOUND);
    }

    @Override
    public void deleteUser(String login) {
        User user = userRepository.findById(login).orElseThrow(USER_NOT_FOUND);
        userRepository.delete(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::mapDomainToDto).toList();
    }

    @Override
    public List<ResourceDto> getUserResources(String login) {
        var user = userRepository.findById(login).orElseThrow(USER_NOT_FOUND);
        var resources = user.resources();
        return resources.stream()
                .map(resourceMapper::mapDomainToDto)
                .toList();
    }
}