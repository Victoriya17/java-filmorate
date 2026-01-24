package ru.yandex.practicum.filmorate.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<UserDto> findAllUsers() {
        log.debug("Получение списка пользователей");
        return userStorage.findAllUsers()
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public UserDto createUser(NewUserRequest request) {
        log.debug("Создание нового пользователя {}", request.getName());
        validateUniqueEmail(request.getEmail());

        User user = UserMapper.mapToUser(request);
        user = userStorage.createUser(user);

        return UserMapper.mapToUserDto(user);
    }

    public UserDto updateUser(UpdateUserRequest request) {
        User existingUser = userStorage.findUserById(request.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (request.getEmail() != null && !request.getEmail().equals(existingUser.getEmail())) {
            validateUniqueEmail(request.getEmail());
        }
        User updatedUser = UserMapper.updateUserFields(existingUser, request);
        updatedUser = userStorage.updateUser(updatedUser);

        return UserMapper.mapToUserDto(updatedUser);
    }

    private void validateUniqueEmail(String email) {
        userStorage.findUserByEmail(email)
                .ifPresent(user -> {
                    throw new DuplicatedDataException("Этот имейл уже используется.");
                });
    }

    public UserDto findUserById(Long id) {
        log.debug("Поиск пользователя c ID{}", id);
        User user = userStorage.findUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));

        return UserMapper.mapToUserDto(user);
    }

    @Transactional
    public void addFriend(Long id, Long friendId) {
        log.debug("Пользователь {} отправляет заявку в друзья пользователю {}", id, friendId);

        User user = userStorage.findUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
        userStorage.findUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + friendId + " не найден"));

        boolean isAdded = userStorage.tryAddFriendship(id, friendId);

        try {
        if (!isAdded) {
            log.warn("Пользователь {} уже в друзьях у {}", friendId, id);
            return;
        }

        user.getFriends().add(friendId);
        log.info("Пользователь {} добавил в друзья пользователя {}", id, friendId);

        } catch (DataAccessException e) {
            log.error("Ошибка при добавлении дружбы между {} и {}", id, friendId, e);
            throw new InternalServerException("Не удалось добавить дружбу");
        }
    }

    public void removeFriend(Long id, Long friendId) {
        log.debug("Пользователь {} удаляет из друзей пользователя {}", id, friendId);

        User user = userStorage.findUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
        userStorage.findUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + friendId + " не найден"));

        if (!user.getFriends().contains(friendId)) {
            return;
        }

        boolean isRemoved = userStorage.removeFriendship(id, friendId);

        if (!isRemoved) {
            throw new InternalServerException("Не удалось удалить дружбу из БД");
        }

        log.debug("Пользователь {} успешно удалён из друзей пользователя {}", friendId, id);
    }

    public Collection<UserDto> getFriends(Long id) {
        if (id == null) {
            throw new ValidationException("Id пользователя должен быть указан.");
        }
        User user = userStorage.findUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));

        log.debug("Получение списка друзей пользователя " + user.getName());

        Set<Long> friendsIds = user.getFriends();
        if (friendsIds.isEmpty()) {
            log.trace("У пользователя " + user.getName() + " нет друзей");
            return new ArrayList<>();
        }

        List<UserDto> friendsDtos = new ArrayList<>();
        for (Long friendId : friendsIds) {
            Optional<User> friendOpt = userStorage.findUserById(friendId);
            if (friendOpt.isPresent()) {
                UserDto friendDto = UserMapper.mapToUserDto(friendOpt.get());
                friendsDtos.add(friendDto);
            } else {
                log.warn("Друг с ID {} не найден и будет пропущен", friendId);
            }
        }

        log.trace("Список друзей пользователя " + user.getName());
        return friendsDtos;
    }

    public Collection<UserDto> getCommonFriends(Long id, Long otherId) {
        log.debug("Получение списка общих друзей двух пользователей с ID={} и ID={}", id, otherId);
        Collection<UserDto> userFriends = getFriends(id);
        Collection<UserDto> otherUserFriends = getFriends(otherId);

        Collection<UserDto> commonFriends = userFriends.stream()
                .filter(otherUserFriends::contains)
                .collect(Collectors.toList());

        log.trace("Найдено {} общих друзей для пользователей ID={} и ID={}",
                commonFriends.size(), id, otherId);
        return commonFriends;
    }
}
