package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class GenreDbStorage extends BaseDbStorage<Genre> implements GenreStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM genres";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE genre_id = ?";
    private static final String FIND_GENRES_BY_FILM_ID = "SELECT g.* FROM genres g " +
                    "JOIN film_genres fg ON g.genre_id = fg.genre_id WHERE fg.film_id = ?";

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper, Genre.class);
    }

    @Override
    public List<Genre> findAllGenres() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Genre> findGenreById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public Optional<Set<Genre>> findGenresByFilmId(Long filmId) {
        try {
            List<Genre> genres = jdbc.query(FIND_GENRES_BY_FILM_ID, new GenreRowMapper(), filmId);
            return Optional.of(new LinkedHashSet<>(genres));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
