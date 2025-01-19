package ru.nextupvamp.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nextupvamp.model.entities.User;
import ru.nextupvamp.repository.UserRepository;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@AllArgsConstructor
public class UserService {
    private static final Supplier<NoSuchElementException> USER_NOT_FOUND =
            () -> new NoSuchElementException("User Not Found");

    private UserRepository userRepository;

    public Optional<User> addNewUser(User userData) {
        User user = new User();
        user.login(userData.login());
        user.password(userData.password());
        if (userRepository.findById(user.login()).isPresent()) {
            return Optional.empty();
        }

        return Optional.of(userRepository.save(user));
    }

    public User getUserByLogin(String login) {
        return userRepository.findById(login).orElseThrow(USER_NOT_FOUND);
    }

    public void deleteUser(String login) {
        User user = userRepository.findById(login).orElseThrow(USER_NOT_FOUND);
        userRepository.delete(user);
    }
}