package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserStorageTest {
    private User user;
    private User existingUser;
    private User updatedUser;
    private InMemoryUserStorage inMemoryUserStorage;

    @BeforeEach
    void beforeEach() {
        user = new User();
        inMemoryUserStorage = new InMemoryUserStorage();
    }

    @Test
    void shouldReturnAllUsersFromStorage() {
        user.setEmail("user1@example.com");
        user.setLogin("user1");
        user.setName("User One");
        user.setBirthday(LocalDate.of(1990, 5, 15));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1995, 8, 20));

        inMemoryUserStorage.createUser(user);
        inMemoryUserStorage.createUser(user2);


        Collection<User> allUsers = inMemoryUserStorage.findAllUsers();

        assertNotNull(allUsers, "Коллекция не должна быть null");
        assertEquals(2, allUsers.size(), "Должно вернуться 2 пользователя");
    }

    @Test
    void shouldReturnEmptyCollectionWhenNoUsers() {
        Collection<User> allUsers = inMemoryUserStorage.findAllUsers();

        assertNotNull(allUsers, "Коллекция не должна быть null при пустом хранилище");
        assertTrue(allUsers.isEmpty(), "При отсутствии пользователей коллекция должна быть пустой");
        assertEquals(0, allUsers.size(), "Размер коллекции должен быть 0");
    }

    @Test
    void testDuplicatedEmail() {
        String testEmail = "test@mail.ru";
        user.setEmail(testEmail);
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(1994, 5, 17));
        inMemoryUserStorage.createUser(user);

        User user2 = new User();
        user2.setEmail(testEmail);

        DuplicatedDataException e = assertThrows(DuplicatedDataException.class,
                () -> inMemoryUserStorage.createUser(user2));
        assertEquals("Этот имейл уже используется.", e.getMessage());


        assertEquals(1, inMemoryUserStorage.findAllUsers().stream()
                .filter(u -> testEmail.equals(u.getEmail()))
                .count(), "");
    }

    @Test
    void shouldSetNameToLoginWhenNameIsNull() {
        user.setLogin("JhonnyDepp");
        user.setName(null);
        user.setEmail("test@email.ru");
        user.setBirthday(LocalDate.of(1994, 5, 17));

        User result = inMemoryUserStorage.createUser(user);

        assertEquals("JhonnyDepp", result.getName(), "Имя должно быть равным логину, если name = null");
    }

    @Test
    void shouldSetNameToLoginWhenNameIsBlank() {
        user.setLogin("JhonnyDepp");
        user.setName("");
        user.setEmail("test@email.ru");
        user.setBirthday(LocalDate.of(1994, 5, 17));

        User result = inMemoryUserStorage.createUser(user);

        assertEquals("JhonnyDepp", result.getName(), "Имя должно быть равным логину, если name пуст");
    }

    @Test
    void shouldNotChangeNameWhenNameIsProvided() {
        user.setLogin("jhonny");
        user.setName("JhonnyDepp");
        user.setEmail("test@email.ru");
        user.setBirthday(LocalDate.of(1994, 5, 17));

        User result = inMemoryUserStorage.createUser(user);

        assertEquals("JhonnyDepp", result.getName(), "Имя не должно изменяться, если оно уже задано");
    }

    @Test
    void testBirthdayNotInFuture() {
        user.setLogin("login");
        user.setEmail("test@email.ru");
        user.setBirthday(LocalDate.of(2030, 5, 17));

        ValidationException e = assertThrows(ValidationException.class, () -> inMemoryUserStorage.createUser(user));
        assertEquals("Дата рождения не может быть равна нулю или быть в будущем.", e.getMessage());
    }

    @Test
    void testBirthdayInPast() {
        user.setBirthday(LocalDate.of(1994, 5, 17));
        user.setEmail("test@email.ru");
        user.setLogin("Depp");

        User createdUser = inMemoryUserStorage.createUser(user);

        assertEquals(LocalDate.of(1994, 5, 17), createdUser.getBirthday(),
                "День Рождения должен быть в прошлом");
    }

    private void beforeUpdateUserTest() {
        existingUser = new User();
        existingUser.setId(2L);
        existingUser.setEmail("old@email.com");
        existingUser.setLogin("oldlogin");
        existingUser.setName("Old Name");
        existingUser.setBirthday(LocalDate.of(1990, 1, 1));

        inMemoryUserStorage.createUser(existingUser);

        updatedUser = new User();
        updatedUser.setId(existingUser.getId());
        updatedUser.setBirthday(LocalDate.of(1995, 5, 15));
    }

    @Test
    void shouldUpdateAllFieldsSuccessfully() {
        beforeUpdateUserTest();
        updatedUser.setEmail("new@email.com");
        updatedUser.setLogin("newlogin");
        updatedUser.setName("New Name");

        User result = inMemoryUserStorage.updateUser(updatedUser);

        assertEquals("new@email.com", result.getEmail());
        assertEquals("newlogin", result.getLogin());
        assertEquals("New Name", result.getName());
        assertEquals(LocalDate.of(1995, 5, 15), result.getBirthday());
    }

    @Test
    void shouldSetNameToLoginIfNameIsNull() {
        beforeUpdateUserTest();
        updatedUser.setEmail("new@email.com");
        updatedUser.setName(null);
        updatedUser.setLogin("newlogin");

        assertEquals("Old Name", inMemoryUserStorage.updateUser(updatedUser).getName());
    }

    @Test
    void shouldSetNameToLoginIfNameIsBlank() {
        beforeUpdateUserTest();
        updatedUser.setEmail("new@email.com");
        updatedUser.setName("");
        updatedUser.setLogin("newlogin");

        assertEquals("Old Name", inMemoryUserStorage.updateUser(updatedUser).getName());
    }

    @Test
    void shouldNotUpdateFutureBirthday() {
        user.setId(1L);
        user.setEmail("user1@example.com");
        user.setLogin("user1");
        user.setName("User One");
        user.setBirthday(LocalDate.of(1990, 5, 15));
        inMemoryUserStorage.createUser(user);
        User updatedUser2 = new User();
        updatedUser2.setId(user.getId());
        updatedUser2.setEmail("new@email.com");
        updatedUser2.setLogin("newlogin");
        updatedUser2.setBirthday(LocalDate.of(2030, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> inMemoryUserStorage.updateUser(updatedUser2));
        assertEquals("Дата рождения не может быть равна нулю или быть в будущем.", exception.getMessage());
    }
}
