package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> findAllUsers() {
        log.debug("Получение списка пользователей");
        return userStorage.findAllUsers();
    }

    public User createUser(User user) {
        log.debug("Создание нового пользователя {}", user.getName());
        return userStorage.createUser(user);
    }

    public User updateUser(User newUser) {
        log.debug("Обновление пользователя c ID{}", newUser.getId());
        return userStorage.updateUser(newUser);
    }

    public User findUserById(Long id) {
        log.debug("Поиск пользователя c ID{}", id);
        return userStorage.findUserById(id);
    }

    public void addFriend(Long id, Long friendId) {
        User user = findUserById(id);
        User friend = findUserById(friendId);

        if (user.getFriends().contains(friendId) && friend.getFriends().contains(id)) {
            log.warn("Пользователи {} и {} уже в друзьях друг у друга", id, friendId);
            return;
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(id);
        log.info("Установлена взаимная дружба пользователей {} и {}", id, friendId);
    }

    public void removeFriend(Long id, Long friendId) {
        User user = findUserById(id);
        User friend = findUserById(friendId);

        if (user.getFriends().remove(friendId) && friend.getFriends().remove(id)) {
            log.info("Успешно удалена дружба между {} и {}", id, friendId);
        } else {
            log.warn("Дружба между {} и {} не существовала", id, friendId);
        }
    }

    public Collection<User> getFriends(Long id) {
        if (id == null) {
            throw new ValidationException("Id пользователя должен быть указан.");
        }
        User user = findUserById(id);
        log.debug("Получение списка друзей пользователя " + user.getName());
        Set<Long> friendsIds = user.getFriends();

        if (friendsIds.isEmpty()) {
            log.trace("У пользователя " + user.getName() + " нет друзей");
            return new ArrayList<>();
        }

        ArrayList<User> friendsList = new ArrayList<>();
        for (Long friendId : friendsIds) {
            friendsList.add(findUserById(friendId));
        }

        log.trace("Список друзей пользователя " + user.getName());
        return friendsList;
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        log.debug("Получение списка общих друзей двух пользователей с ID={} и ID={}", id, otherId);
        Collection<User> userFriends = getFriends(id);
        Collection<User> otherUserFriends = getFriends(otherId);

        Collection<User> commonFriends = userFriends.stream()
                .filter(otherUserFriends::contains)
                .collect(Collectors.toList());

        log.trace("Найдено {} общих друзей для пользователей ID={} и ID={}",
                commonFriends.size(), id, otherId);
        return commonFriends;
    }
}
