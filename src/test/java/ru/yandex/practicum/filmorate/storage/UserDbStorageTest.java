package ru.yandex.practicum.filmorate.storage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@ComponentScan("ru.yandex.practicum.filmorate")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {
    private final UserDbStorage userStorage;

    @Test
    public void testFindAllUsers() {
        assertThat(userStorage.findAllUsers()).isNotEmpty()
                .hasSize(3)
                .filteredOn("name", "Yellow")
                .isNotEmpty()
                .hasExactlyElementsOfTypes(User.class);
    }

    @Test
    public void testFindUserById() {
        Optional<User> userOptional = userStorage.findUserById(3L);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 3L)
                                .hasFieldOrPropertyWithValue("name", "White")
                );
    }

    @Test
    public void testCreateUser() {
        User newUser = new User();
        newUser.setEmail("black@gmail.ru");
        newUser.setLogin("Black");
        newUser.setName("Black");
        newUser.setBirthday(LocalDate.of(1998, 8, 17));

        assertThat(userStorage.createUser(newUser))
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 4L);
    }

    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setEmail("yellow@gmail.ru");
        user.setLogin("Yellow17");
        user.setName("Yellow");
        user.setBirthday(LocalDate.of(1998, 8, 17));
        user.setId(2L);

        assertThat(userStorage.updateUser(user))
                .isNotNull()
                .hasFieldOrPropertyWithValue("login", "Yellow17");
    }

    @Test
    void testTryAddFriendship() {
        boolean result = userStorage.tryAddFriendship(2L, 3L);

        assertThat(result).isTrue();
    }

    @Test
    void testTryAddFriendshipDuplicate() {
        boolean result = userStorage.tryAddFriendship(3L, 1L);

        assertThat(result).isFalse();
    }

    @Test
    void testTryAddFriendshipUserNull() {
        assertThrows(InternalServerException.class, () -> {
            userStorage.tryAddFriendship(null, 1L);
        });
    }

    @Test
    void testTryAddFriendshipFriendNull() {
        assertThrows(InternalServerException.class, () -> {
            userStorage.tryAddFriendship(1L, null);
        });
    }

    @Test
    void testTryAddFriendshipError() {
        assertThrows(InternalServerException.class, () -> {
            userStorage.tryAddFriendship(999L, 999L);
        });
    }

    @Test
    void testTryRemoveFriendship() {
        boolean result = userStorage.removeFriendship(1L, 2L);

        assertThat(result).isTrue();
    }

    @Test
    public void testFindUserByEmail() {
        Optional<User> userOptional = userStorage.findUserByEmail("green@yandex.ru");

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("email", "green@yandex.ru")
                );
    }
}
