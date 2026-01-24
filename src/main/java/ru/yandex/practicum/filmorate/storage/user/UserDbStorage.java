package ru.yandex.practicum.filmorate.storage.user;

import jakarta.transaction.Transactional;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserWithFriendsRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT u.user_id, u.email, u.login, u.name, u.birthday, " +
            "f.friend_id FROM users u LEFT JOIN friends f ON u.user_id = f.user_id WHERE u.user_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthday)" +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
            "WHERE user_id = ?";
    private static final String CHECK_FRIENDS = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
    private static final String ADD_FRIEND = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE_FRIEND = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_EMAIL = "SELECT * FROM users WHERE email = ?";

    RowMapper<User> userRowMapper;
    RowMapper<User> userWithFriendsRowMapper;

    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper userRowMapper,
                         UserWithFriendsRowMapper userWithFriendsRowMapper) {
        super(jdbc, userRowMapper, User.class);
        this.userRowMapper = userRowMapper;
        this.userWithFriendsRowMapper = userWithFriendsRowMapper;
    }

    @Override
    public List<User> findAllUsers() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<User> findUserById(Long userId) {
        try {
            List<User> users = jdbc.query(
                    FIND_BY_ID_QUERY,
                    new Object[]{userId},
                    userWithFriendsRowMapper
            );
            return users.stream().findFirst();
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public User createUser(User user) {
        long id = insert(
                INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    @Override
    public User updateUser(User user) {
        update(
                UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    @Transactional
    public boolean tryAddFriendship(Long userId, Long friendId) {
        try {
            Integer count = jdbc.queryForObject(
                    CHECK_FRIENDS,
                    Integer.class,
                    userId, friendId);

            if (count != null && count > 0) {
                return false;
            }

            jdbc.update(
                    ADD_FRIEND,
                    userId,
                    friendId);

            return true;
        } catch (DataAccessException e) {
            throw new InternalServerException("Не удалось добавить дружбу");
        }
    }

    public boolean removeFriendship(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            return false;
        }

        try {
            int rows = jdbc.update(
                    DELETE_FRIEND,
                    userId,
                    friendId
            );

            if (rows > 0) {
                return true;
            } else {
                return false;
            }
        } catch (DataAccessException e) {
            throw new InternalServerException("Не удалось удалить дружбу из БД");
        }
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        try {
            return Optional.ofNullable(
                    jdbc.queryForObject(
                            FIND_EMAIL,
                            new Object[]{email},
                            userRowMapper
                    )
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
