package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<UserDto> findAllUsers() {
        return userService.findAllUsers();
    }

    @PostMapping
    public UserDto createUser(@Valid @RequestBody NewUserRequest userRequest) {
        return userService.createUser(userRequest);
    }

    @PutMapping
    public UserDto updateUser(@Valid @RequestBody UpdateUserRequest userRequest) {
        return userService.updateUser(userRequest);
    }

    @GetMapping("/{id}")
    public UserDto findUserById(@PathVariable Long id) {
        return userService.findUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<UserDto> getFriends(@PathVariable Long id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<UserDto> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public boolean deleteUserById(@PathVariable Long id) {
        return userService.deleteById(id);
    }

    @GetMapping("/{id}/recommendations")
    public List<Film> getRecommendations(@PathVariable Long id) {
        return userService.getRecommendations(id);
    }
}