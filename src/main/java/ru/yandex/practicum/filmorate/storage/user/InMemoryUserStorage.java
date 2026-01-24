package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> findAllUsers() {
        return users.values();
    }

    public User createUser(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
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

        users.put(newUser.getId(), newUser);
        return newUser;
    }

    public Optional<User> findUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public boolean tryAddFriendship(Long userId, Long friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);

        if (user == null || friend == null) {
            return false;
        }

        if (user.getFriends().contains(friendId)) {
            return false;
        }

        user.getFriends().add(friendId);
        log.debug("Пользователь {} добавил в друзья {}", userId, friendId);

        return true;
    }

    public boolean removeFriendship(Long userId, Long friendId) {
        User user = users.get(userId);
        if (user == null || !user.getFriends().contains(friendId)) {
            return false;
        }
        user.getFriends().remove(friendId);
        return true;
    }

    public Optional<User> findUserByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }
}
