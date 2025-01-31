package ru.nextupvamp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nextupvamp.model.data.UserDto;
import ru.nextupvamp.model.entities.Resource;
import ru.nextupvamp.model.entities.User;
import ru.nextupvamp.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("users")
@AllArgsConstructor
@Tag(
        name = "User controller",
        description = "Controller is used to manage users and get their information"
)
public class UserController {
    private final UserService userService;

    // unfortunately there's no safety for user data

    @Operation(summary = "Adding a new user")
    @PostMapping("new")
    public ResponseEntity<?> addNewUser(@Valid @RequestBody UserDto user) {
        Optional<User> addedUser = userService.addNewUser(user);
        if (addedUser.isEmpty()) {
            ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
            problemDetail.setTitle("User Already Exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Getting user data such as login and password")
    @GetMapping("{login}")
    public UserDto getUserData(@PathVariable String login) {
        var foundUser = userService.getUserByLogin(login);
        return new UserDto(foundUser.login(), foundUser.password());
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

    // this endpoint should contain something that will verify
    // if user is allowed to get this information... but it doesn't
    @Operation(summary = "Getting user resources list")
    @GetMapping("{login}/resources")
    public List<Resource> getUserResources(@PathVariable String login) {
        return userService.getUserByLogin(login).resources();
    }
}
