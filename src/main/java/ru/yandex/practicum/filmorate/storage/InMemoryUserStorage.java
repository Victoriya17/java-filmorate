package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> findAllUsers() {
        return users.values();
    }

    public User createUser(User user) {
        if (!user.getEmail().contains("@")) { //если оставить @Email не проходил тест в Postman
            throw new ValidationException("Имейл должен содержать @.");
        }

        validateUniqueEmail(user.getEmail());
        validateName(user);
        validateBirthday(user.getBirthday());

        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    private void validateUniqueEmail(String email) {
        for (User user : users.values()) {
            if (user.getEmail().equals(email)) {
                throw new DuplicatedDataException("Этот имейл уже используется.");
            }
        }
    }

    private void validateName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void validateBirthday(LocalDate birthday) {
        if (birthday.isAfter(LocalDate.now())) {
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

    public User updateUser(User newUser) {
        log.info("Начало обновления пользователя. ID: {}", newUser.getId());

        if (newUser.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        if (!users.containsKey(newUser.getId())) {
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }

        User oldUser = users.get(newUser.getId());

        if (newUser.getEmail() != null && !newUser.getEmail().isBlank() &&
                !oldUser.getEmail().equals(newUser.getEmail())) {
            validateUniqueEmail(newUser.getEmail());
            oldUser.setEmail(newUser.getEmail());
        }

        validateLoginOnUpdate(newUser, oldUser);
        updateNameIfProvided(newUser, oldUser);
        validateBirthdayOnUpdate(newUser, oldUser);

        return oldUser;
    }

    private void validateLoginOnUpdate(User newUser, User oldUser) {
        if (newUser.getLogin() != null && !newUser.getLogin().isBlank() &&
                !oldUser.getLogin().equals(newUser.getLogin())) {
            oldUser.setLogin(newUser.getLogin());
        }
    }

    private void updateNameIfProvided(User newUser, User oldUser) {
        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            oldUser.setName(newUser.getName());
        }
    }

    private void validateBirthdayOnUpdate(User newUser, User oldUser) {
        if (newUser.getBirthday() != null && !oldUser.getBirthday().equals(newUser.getBirthday())) {
            validateBirthday(newUser.getBirthday());
            oldUser.setBirthday(newUser.getBirthday());
        }
    }

    public User findUserById(Long id) {
        if (users.get(id) == null) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return users.get(id);
    }
}
