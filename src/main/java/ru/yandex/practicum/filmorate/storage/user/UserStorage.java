package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> findAllUsers();

    User createUser(User user);

    User updateUser(User newUser);

    Optional<User> findUserById(Long id);

    boolean tryAddFriendship(Long userId, Long friendId);

    boolean removeFriendship(Long userId, Long friendId);

    Optional<User> findUserByEmail(String email);
}
