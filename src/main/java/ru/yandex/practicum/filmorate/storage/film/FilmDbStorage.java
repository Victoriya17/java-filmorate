package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.util.*;

@Repository
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private static final String FIND_ALL_QUERY = "SELECT DISTINCT f.film_id, f.name, f.description, f.releaseDate, f.duration," +
            " r.id AS rating_id, r.name AS rating_name FROM films f LEFT JOIN ratings r ON f.rating_id = r.id " +
            "LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id LEFT JOIN genres AS g ON fg.genre_id = g.genre_id";
    private static final String FIND_BY_ID_QUERY = "SELECT f.film_id, f.name, f.description, f.releaseDate, f.duration," +
            " r.id AS rating_id, r.name AS rating_name FROM films f LEFT JOIN ratings r ON f.rating_id = r.id " +
            "WHERE f.film_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO films(name, description, releaseDate, duration, rating_id)" +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, releaseDate = ?, duration " +
            "= ?, rating_id = ? WHERE film_id = ?";
    private static final String ADD_FILM_GENRE = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String FIND_LIKES = "SELECT user_id FROM film_likes WHERE film_id = ?";
    private static final String ADD_LIKES = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_LIKE = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String GET_POPULAR = "SELECT f.*, r.name AS rating_name FROM films AS f " +
            "INNER JOIN ratings AS r ON f.rating_id = r.id INNER JOIN ( SELECT l.film_id, COUNT(l.user_id) AS likes " +
            "FROM film_likes AS l GROUP BY l.film_id ORDER BY COUNT(l.user_id) DESC LIMIT ? ) AS liked_films " +
            "ON f.film_id = liked_films.film_id ORDER BY liked_films.likes DESC";

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper, Film.class);
    }

    @Override
    public Collection<Film> findAllFilms() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Film createFilm(Film film) {
        long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId(id);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        return film;
    }

    @Override
    public Optional<Film> findFilmById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public void addFilmGenres(Long filmId, Collection<Long> genreIds) {
        List<Object[]> batchArgs = new ArrayList<>();
        for (Long genreId : genreIds) {
            batchArgs.add(new Object[]{filmId, genreId});
        }
        jdbc.batchUpdate(ADD_FILM_GENRE, batchArgs);
    }

    @Override
    public Set<Long> findAllLikes(Film film) {
        List<Long> likes = jdbc.queryForList(FIND_LIKES, Long.class, film.getId());
        return new HashSet<>(likes);
    }

    @Override
    public void addLike(Long id, Long userId) {
        int countOfLikes = jdbc.update(ADD_LIKES, id, userId);

        if (countOfLikes == 0) {
            throw new DuplicatedDataException("Лайк уже поставлен");
        }
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
            jdbc.update(REMOVE_LIKE, filmId, userId);
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count должен быть > 0");
        }
        return jdbc.query(GET_POPULAR, new FilmRowMapper(), count);
    }
}
