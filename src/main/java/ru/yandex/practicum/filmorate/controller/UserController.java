package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAllUsers() {
        return users.values();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        if (user == null) {
            throw new ValidationException("Пользователь не может быть null");
        }

        validateUniqueEmail(user);
        validateLogin(user);
        validateName(user);
        validateBirthday(user);

        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    private void validateUniqueEmail(User user) {
        for (User existingUser : users.values()) {
            if (existingUser.getEmail().equals(user.getEmail())) {
                throw new DuplicatedDataException("Этот имейл уже используется.");
            }
        }
    }

    private void validateLogin(User user) {
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин должен быть указан без пробелов и не должен быть пустым.");
        }
    }

    private void validateName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void validateBirthday(User user) {
        if (user.getBirthday().isAfter(LocalDate.now())) {
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
    public User updateUser(@Valid @RequestBody User newUser) {
        log.info("Начало обновления пользователя. ID: {}", newUser.getId());

        if (newUser.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        if (!users.containsKey(newUser.getId())) {
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }

        User oldUser = users.get(newUser.getId());

        validateLogin(newUser);
        validateBirthday(newUser);

        if (!oldUser.getEmail().equals(newUser.getEmail())) {
            validateUniqueEmail(newUser);
        }

        oldUser.setEmail(newUser.getEmail());
        oldUser.setLogin(newUser.getLogin());
        updateNameIfProvided(newUser, oldUser);
        oldUser.setBirthday(newUser.getBirthday());

        return oldUser;
    }

    private void updateNameIfProvided(User newUser, User oldUser) {
        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            oldUser.setName(newUser.getName());
        } else {
            oldUser.setName(newUser.getLogin());
        }
    }
}
