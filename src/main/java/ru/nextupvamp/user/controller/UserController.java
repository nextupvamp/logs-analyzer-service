package ru.nextupvamp.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.nextupvamp.resource.dto.ResourceDto;
import ru.nextupvamp.user.dto.LoginResponse;
import ru.nextupvamp.user.dto.UserDto;
import ru.nextupvamp.user.service.UserServiceImpl;

import java.util.List;

@RestController
@RequestMapping("users")
@AllArgsConstructor
@Tag(
        name = "User controller",
        description = "Controller is used to manage users and get their information"
)
public class UserController {

    private final UserServiceImpl userService;

    @Operation(summary = "Adding a new user")
    @PostMapping("new")
    public LoginResponse addNewUser(@Valid @RequestBody UserDto user) {
        return userService.addNewUser(user);
    }

    @Operation(summary = "Getting user data such as login and password")
    @GetMapping("{login}")
    public UserDto getUserData(@PathVariable String login) {
        return userService.getUserByLogin(login);
    }

    @Operation(summary = "Getting data on all the users")
    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "Deleting a user")
    @DeleteMapping("{login}")
    public void deleteUser(@PathVariable String login) {
        userService.deleteUser(login);
    }

    @Operation(summary = "Getting user resources list")
    @GetMapping("{login}/resources")
    public List<ResourceDto> getUserResources(@PathVariable String login) {
        return userService.getUserResources(login);
    }
}
