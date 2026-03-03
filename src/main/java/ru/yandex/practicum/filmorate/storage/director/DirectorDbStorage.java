package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.List;
import java.util.Optional;

@Repository
public class DirectorDbStorage extends BaseDbStorage<Director> implements DirectorStorage {

    private static final String CREATE_DIRECTOR = "INSERT INTO directors (name) VALUES (?)";
    private static final String UPDATE_DIRECTOR = "UPDATE directors SET name = ? WHERE id = ?";
    private static final String GET_DIRECTOR_BY_ID = "SELECT id, name FROM directors WHERE id = ?";
    private static final String GET_ALL_DIRECTORS = "SELECT id, name FROM directors ORDER BY id";
    private static final String DELETE_DIRECTOR = "DELETE FROM directors WHERE id = ?";

    public DirectorDbStorage(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper, Director.class);
    }

    @Override
    public Director create(Director director) {
        long id = insert(
                CREATE_DIRECTOR,
                director.getName()
        );
        director.setId(id);
        return director;
    }

    @Override
    public Director update(Director director) {
        update(UPDATE_DIRECTOR,
                director.getName(),
                director.getId());

        return director;
    }

    @Override
    public Optional<Director> getById(Long id) {
        return findOne(GET_DIRECTOR_BY_ID, id);
    }

    @Override
    public List<Director> getAll() {
        return findMany(GET_ALL_DIRECTORS);
    }

    @Override
    public boolean deleteById(Long id) {
        return delete(DELETE_DIRECTOR, id);
    }
}
