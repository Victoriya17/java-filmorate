package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping
    public Collection<User> findAllUsers() {
        return users.values();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        if (user == null) {
            throw new ValidationException("Пользователь не может быть null");
        }

        validateEmail(user);
        validateUniqueEmail(user);
        validateLogin(user);
        validateName(user);
        validateBirthday(user);

        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    private void validateEmail(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Имейл должен быть указан и содержать @.");
        }
    }

    private void validateUniqueEmail(User user) {
        for (User existingUser : users.values()) {
            if (existingUser.getEmail().equals(user.getEmail())) {
                throw new DuplicatedDataException("Этот имейл уже используется.");
            }
        }
    }

    private void validateLogin(User user) {
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин должен быть указан без пробелов и не должен быть пустым.");
        }
    }

    private void validateName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void validateBirthday(User user) {
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть равна нулю или быть в будущем.");
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
        log.info("Начало обновления пользователя. ID: {}", newUser.getId());

        if (newUser.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            validateUniqueEmail(newUser);
            updateEmailIfValid(newUser, oldUser);
            updateLoginIfValid(newUser, oldUser);
            updateNameIfProvided(newUser, oldUser);
            updateBirthdayIfValid(newUser, oldUser);

            return oldUser;
        }
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    private void updateEmailIfValid(User newUser, User oldUser) {
        if (newUser.getEmail() != null && !newUser.getEmail().isBlank() && newUser.getEmail().contains("@")) {
            oldUser.setEmail(newUser.getEmail());
        }
    }

    private void updateLoginIfValid(User newUser, User oldUser) {
        if (newUser.getLogin() != null && !newUser.getLogin().isBlank() && !newUser.getLogin().contains(" ")) {
            oldUser.setLogin(newUser.getLogin());
        }
    }

    private void updateNameIfProvided(User newUser, User oldUser) {
        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            oldUser.setName(newUser.getName());
        } else {
            oldUser.setName(newUser.getLogin());
        }
    }

    private void updateBirthdayIfValid(User newUser, User oldUser) {
        if (newUser.getBirthday() != null && newUser.getBirthday().isBefore(LocalDate.now())) {
            oldUser.setBirthday(newUser.getBirthday());
        }
    }

}