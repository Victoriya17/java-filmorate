package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

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

    public Collection<User> findCommonFriends(Long userId, Long otherUserId) {
        log.debug("Поиск общих друзей для пользователей ID={} и ID={}", userId, otherUserId);

        User user = users.get(userId);
        User otherUser = users.get(otherUserId);

        if (user == null || otherUser == null) {
            log.warn("Один из пользователей не найден: ID={} или ID={}", userId, otherUserId);
            return Collections.emptyList();
        }

        Set<Long> userFriends = new HashSet<>(user.getFriends());
        Set<Long> otherUserFriends = new HashSet<>(otherUser.getFriends());

        Set<Long> commonFriendIds = userFriends.stream()
                .filter(otherUserFriends::contains)
                .collect(Collectors.toSet());

        Collection<User> commonFriends = commonFriendIds.stream()
                .map(friendId -> users.get(friendId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.trace("Найдено {} общих друзей для ID={} и ID={}",
                commonFriends.size(), userId, otherUserId);

        return commonFriends;
    }

    @Override
    public Collection<User> findFriendsByUserId(Long userId) {
        log.debug("Поиск друзей пользователя с ID={}", userId);

        User user = users.get(userId);
        if (user == null) {
            log.warn("Пользователь с ID={} не найден", userId);
            return Collections.emptyList();
        }

        Set<Long> friendIds = user.getFriends();
        if (friendIds.isEmpty()) {
            log.trace("У пользователя с ID={} нет друзей", userId);
            return Collections.emptyList();
        }

        Collection<User> friends = friendIds.stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.trace("Для пользователя с ID={} найдено {} друзей", userId, friends.size());
        return friends;
    }
}
