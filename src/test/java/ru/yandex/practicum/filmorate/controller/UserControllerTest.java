package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private User user;
    private User existingUser;
    private User updatedUser;
    private UserController userController;

    @BeforeEach
    void beforeEach() {
        user = new User();
        userController = new UserController();
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

        userController.createUser(user);
        userController.createUser(user2);


        Collection<User> allUsers = userController.findAllUsers();

        assertNotNull(allUsers, "Коллекция не должна быть null");
        assertEquals(2, allUsers.size(), "Должно вернуться 2 пользователя");
    }

    @Test
    void shouldReturnEmptyCollectionWhenNoUsers() {
        Collection<User> allUsers = userController.findAllUsers();

        assertNotNull(allUsers, "Коллекция не должна быть null при пустом хранилище");
        assertTrue(allUsers.isEmpty(), "При отсутствии пользователей коллекция должна быть пустой");
        assertEquals(0, allUsers.size(), "Размер коллекции должен быть 0");
    }

    private void assertEmailThrowsValidationException(String email) {
        user.setLogin("testlogin");
        user.setEmail(email);
        user.setBirthday(LocalDate.of(1994, 5, 17));
        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void testEmailWithoutAt() {
        assertEmailThrowsValidationException("testmail.com");
    }

    @Test
    void testEmailNull() {
        assertEmailThrowsValidationException(null);
    }

    @Test
    void testEmailIsBlank() {
        assertEmailThrowsValidationException("");
    }

    @Test
    void testDuplicatedEmail() {
        String testEmail = "test@mail.ru";
        user.setEmail(testEmail);
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(1994, 5, 17));
        userController.createUser(user);

        User user2 = new User();
        user2.setEmail(testEmail);

        assertThrows(DuplicatedDataException.class, () -> {
            userController.createUser(user2);
        });

        assertEquals(1, userController.findAllUsers().stream()
                .filter(u -> testEmail.equals(u.getEmail()))
                .count(), "");
    }

    private void assertLoginThrowsValidationException(String login) {
        user.setLogin(login);
        user.setEmail("test@email.ru");
        user.setBirthday(LocalDate.of(1994, 5, 17));
        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void testLoginWithSpace() {
        assertLoginThrowsValidationException("test login");
    }

    @Test
    void testLoginNull() {
        assertLoginThrowsValidationException(null);
    }

    @Test
    void testLoginIsBlank() {
        assertLoginThrowsValidationException("");
    }

    @Test
    void shouldSetNameToLoginWhenNameIsNull() {
        user.setLogin("JhonnyDepp");
        user.setName(null);
        user.setEmail("test@email.ru");
        user.setBirthday(LocalDate.of(1994, 5, 17));

        User result = userController.createUser(user);

        assertEquals("JhonnyDepp", result.getName(), "Имя должно быть равным логину, если name = null");
    }

    @Test
    void shouldSetNameToLoginWhenNameIsBlank() {
        user.setLogin("JhonnyDepp");
        user.setName("");
        user.setEmail("test@email.ru");
        user.setBirthday(LocalDate.of(1994, 5, 17));

        User result = userController.createUser(user);

        assertEquals("JhonnyDepp", result.getName(), "Имя должно быть равным логину, если name пуст");
    }

    @Test
    void shouldNotChangeNameWhenNameIsProvided() {
        user.setLogin("jhonny");
        user.setName("JhonnyDepp");
        user.setEmail("test@email.ru");
        user.setBirthday(LocalDate.of(1994, 5, 17));

        User result = userController.createUser(user);

        assertEquals("JhonnyDepp", result.getName(), "Имя не должно изменяться, если оно уже задано");
    }

    private void assertBirthdayThrowsValidationException(LocalDate date) {
        user.setLogin("login");
        user.setEmail("test@email.ru");
        user.setBirthday(date);

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void testBirthdayIsNull() {
        assertBirthdayThrowsValidationException(null);
    }

    @Test
    void testBirthdayNotInFuture() {
        assertBirthdayThrowsValidationException(LocalDate.of(2030, 5, 17));
    }

    @Test
    void testBirthdayInPast() {
        user.setBirthday(LocalDate.of(1994, 5, 17));
        user.setEmail("test@email.ru");
        user.setLogin("Depp");

        assertEquals(LocalDate.of(1994, 5, 17), user.getBirthday(),
                "День Рождения должен быть в прошлом");
    }

    private void beforeUpdateUserTest() {
        existingUser = new User();
        existingUser.setId(2L);
        existingUser.setEmail("old@email.com");
        existingUser.setLogin("oldlogin");
        existingUser.setName("Old Name");
        existingUser.setBirthday(LocalDate.of(1990, 1, 1));

        userController.createUser(existingUser);

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

        User result = userController.updateUser(updatedUser);

        assertEquals("new@email.com", result.getEmail());
        assertEquals("newlogin", result.getLogin());
        assertEquals("New Name", result.getName());
        assertEquals(LocalDate.of(1995, 5, 15), result.getBirthday());
    }

    private void assertEmailUpdateThrowsValidationException(String email) {
        beforeUpdateUserTest();
        updatedUser.setLogin("testlogin");
        updatedUser.setEmail(email);
        assertThrows(ValidationException.class, () -> userController.updateUser(user));
    }

    @Test
    void shouldRejectEmailNull() {
        assertEmailUpdateThrowsValidationException(null);
    }

    @Test
    void shouldKeepOldEmailIfNewIsBlank() {
        assertEmailUpdateThrowsValidationException("");
    }

    @Test
    void shouldRejectEmailWithNoAt() {
        assertEmailUpdateThrowsValidationException("invalid-email.com");
    }

    private void assertLoginThrowsValidationExceptionIfUpdate(String login) {
        user.setLogin(login);
        user.setEmail("test@email.ru");
        user.setBirthday(LocalDate.of(1994, 5, 17));
        assertThrows(ValidationException.class, () -> userController.updateUser(user));
    }


    @Test
    void shouldRejectLoginNull() {
        beforeUpdateUserTest();
        assertLoginThrowsValidationExceptionIfUpdate(null);
    }

    @Test
    void shouldKeepOldLoginIfNewIsBlank() {
        beforeUpdateUserTest();
        assertLoginThrowsValidationExceptionIfUpdate("");
    }


    @Test
    void shouldRejectLoginIsValid() {
        beforeUpdateUserTest();
        assertLoginThrowsValidationExceptionIfUpdate("   ");
    }

    @Test
    void shouldSetNameToLoginIfNameIsNull() {
        beforeUpdateUserTest();
        updatedUser.setEmail("new@email.com");
        updatedUser.setName(null);
        updatedUser.setLogin("newlogin");

        assertEquals("newlogin", userController.updateUser(updatedUser).getName());
    }

    @Test
    void shouldSetNameToLoginIfNameIsBlank() {
        beforeUpdateUserTest();
        updatedUser.setEmail("new@email.com");
        updatedUser.setName("");
        updatedUser.setLogin("newlogin");

        assertEquals("newlogin", userController.updateUser(updatedUser).getName());
    }

    @Test
    void shouldNotUpdateFutureBirthday() {
        user.setId(1L);
        user.setEmail("user1@example.com");
        user.setLogin("user1");
        user.setName("User One");
        user.setBirthday(LocalDate.of(1990, 5, 15));
        userController.createUser(user);
        User updatedUser2 = new User();
        updatedUser2.setId(user.getId());
        updatedUser2.setEmail("new@email.com");
        updatedUser2.setLogin("newlogin");
        updatedUser2.setBirthday(LocalDate.of(2030, 1, 1));

        User result = userController.updateUser(updatedUser2);
        assertEquals(LocalDate.of(1990, 5, 15), result.getBirthday());
    }

    @Test
    void shouldNotUpdateBirthdayNull() {
        user.setId(1L);
        user.setEmail("user1@example.com");
        user.setLogin("user1");
        user.setName("User One");
        user.setBirthday(LocalDate.of(1990, 5, 15));
        userController.createUser(user);
        User updatedUser2 = new User();
        updatedUser2.setId(user.getId());
        updatedUser2.setEmail("new@email.com");
        updatedUser2.setLogin("newlogin");
        updatedUser2.setBirthday(null);

        User result = userController.updateUser(updatedUser2);
        assertEquals(LocalDate.of(1990, 5, 15), result.getBirthday());
    }

    @Test
    void shouldThrowValidationExceptionWhenUserIsNull() {
        assertThrows(ValidationException.class, () -> {
            userController.createUser(null);
        });
    }

    @Test
    void shouldThrowValidationExceptionWhenAllFieldsNull() {
        assertThrows(ValidationException.class, () -> {
            userController.createUser(user);
        });
    }
}
