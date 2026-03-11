package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;

import java.util.*;

@Repository
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private static final String FIND_ALL_QUERY = "SELECT DISTINCT f.film_id, f.name, f.description, f.releaseDate, " +
            "f.duration, r.id AS rating_id, r.name AS rating_name " +
            "FROM films f LEFT JOIN ratings r ON f.rating_id = r.id " +
            "LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id " +
            "LEFT JOIN genres AS g ON fg.genre_id = g.genre_id";
    private static final String FIND_BY_ID_QUERY = "SELECT f.film_id, f.name, f.description, f.releaseDate, " +
            "f.duration, r.id AS rating_id, r.name AS rating_name " +
            "FROM films f " +
            "LEFT JOIN ratings r ON f.rating_id = r.id " +
            "WHERE f.film_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO films(name, description, releaseDate, duration, rating_id)" +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, releaseDate = ?, " +
            "duration = ?, rating_id = ? WHERE film_id = ?";
    private static final String ADD_FILM_GENRE = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String FIND_LIKES = "SELECT user_id FROM film_likes WHERE film_id = ?";
    private static final String ADD_LIKES = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_LIKE = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String GET_POPULAR = "SELECT f.*, r.name AS rating_name " +
            "FROM films AS f " +
            "JOIN ratings AS r ON f.rating_id = r.id " +
            "LEFT JOIN film_likes AS l ON f.film_id = l.film_id " +
            "LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id " +
            "WHERE (? IS NULL OR fg.genre_id = ?) " +
            "  AND (? IS NULL OR EXTRACT(YEAR FROM f.releaseDate) = ?) " +
            "GROUP BY f.film_id, r.name " +
            "ORDER BY COUNT(l.user_id) DESC " +
            "LIMIT ?";
    private static final String SAVE_DIRECTORS = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
    private static final String UPDATE_DIRECTORS = "DELETE FROM film_directors WHERE film_id = ?";
    private static final String FILM_DIRECTORS = "SELECT d.id, d.name FROM directors d JOIN film_directors fd ON " +
            "d.id = fd.director_id WHERE fd.film_id = ?";
    private static final String DIRECTOR_SORT_YEAR = "SELECT f.film_id, f.name, f.description, f.releaseDate, " +
            "f.duration, f.rating_id, r.name AS rating_name, d.id AS director_id, d.name AS director_name " +
            "FROM films f " +
            "LEFT JOIN ratings r ON f.rating_id = r.id " +
            "JOIN film_directors fd ON f.film_id = fd.film_id " +
            "LEFT JOIN directors d ON fd.director_id = d.id " +
            "WHERE fd.director_id = ? " +
            "ORDER BY YEAR(f.releaseDate) ASC";
    private static final String DIRECTOR_SORT_LIKES = "SELECT f.film_id, f.name, f.description, f.releaseDate, " +
            "f.duration, f.rating_id, r.name AS rating_name, d.id AS director_id, d.name AS director_name, " +
            "COUNT(l.user_id) AS like_count " +
            "FROM film_directors AS fd " +
            "LEFT JOIN films AS f ON fd.film_id = f.film_id " +
            "LEFT JOIN film_likes AS l ON l.film_id = f.film_id " +
            "JOIN directors AS d ON fd.director_id = d.id " +
            "LEFT JOIN ratings AS r ON f.rating_id = r.id " +
            "WHERE fd.director_id = ? " +
            "GROUP BY f.film_id, d.id " +
            "ORDER BY like_count DESC";
    private static final String DELETE_FILM = "DELETE FROM films WHERE film_id = ?";
    private static final String GET_RECOMMENDATIONS = "SELECT f.film_id, f.name, f.description, f.releaseDate, " +
            "f.duration, f.rating_id, r.name AS rating_name " +
            "FROM films f " +
            "JOIN ratings r ON f.rating_id = r.id " +
            "JOIN film_likes l ON f.film_id = l.film_id " +
            "WHERE l.user_id IN (SELECT DISTINCT l2.user_id " +
            "    FROM film_likes l1 " +
            "    JOIN film_likes l2 ON l1.film_id = l2.film_id " +
            "    WHERE l1.user_id = ? AND l2.user_id != ? ) " +
            "AND f.film_id NOT IN (SELECT film_id FROM film_likes WHERE user_id = ?) " +
            "GROUP BY f.film_id, r.name " +
            "ORDER BY COUNT(l.user_id) DESC " +
            "LIMIT 10;";
    private static final String LIKES_COUNT = "SELECT COUNT(*) FROM film_likes WHERE user_id = ?";
    private static final String GET_COMMON_FILMS = "SELECT f.film_id, f.name, f.description, f.releaseDate, " +
            "f.duration, f.rating_id, r.name AS rating_name " +
            "FROM films f " +
            "JOIN film_likes l1 ON f.film_id=l1.film_id JOIN film_likes l2 ON f.film_id=l2.film_id " +
            "JOIN ratings r ON f.rating_id=r.id " +
            "WHERE l1.user_id=? AND l2.user_id=? " +
            "ORDER BY (SELECT COUNT(*) FROM film_likes fl WHERE fl.film_id=f.film_id) DESC";

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
        saveDirectors(film);

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

        updateFilmDirectors(film);

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
    public Collection<Film> getPopularFilms(int count, Long genreId, Integer year) {
        if (count <= 0) {
            throw new IllegalArgumentException("count должен быть > 0");
        }
        return jdbc.query(GET_POPULAR, mapper, genreId, genreId, year, year, count);
    }

    private void saveDirectors(Film film) {
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            List<Object[]> batchArgs = film.getDirectors().stream()
                    .map(d -> new Object[]{film.getId(), d.getId()})
                    .toList();
            jdbc.batchUpdate(SAVE_DIRECTORS, batchArgs);
        }
    }

    private void updateFilmDirectors(Film film) {
        jdbc.update(UPDATE_DIRECTORS, film.getId());
        saveDirectors(film);
    }

    private Set<Director> getDirectorsByFilmId(Long filmId) {
        return new HashSet<>(jdbc.query(FILM_DIRECTORS, new DirectorRowMapper(), filmId));
    }

    @Override
    public List<Film> getFilmsByDirectorIdSortedByYear(Long id) {
        List<Film> films = jdbc.query(DIRECTOR_SORT_YEAR, mapper, id);
        films.forEach(film -> film.setDirectors(getDirectorsByFilmId(film.getId())));
        return films;
    }

    @Override
    public List<Film> getFilmsByDirectorIdSortedByLikes(Long id) {
        List<Film> films = jdbc.query(DIRECTOR_SORT_LIKES, mapper, id);
        films.forEach(film -> film.setDirectors(getDirectorsByFilmId(film.getId())));
        return films;
    }

    @Override
    public boolean deleteById(Long id) {
        return delete(DELETE_FILM, id);
    }

    @Override
    public List<Film> getRecommendations(Long userId) {
        Integer likesCount = jdbc.queryForObject(LIKES_COUNT, Integer.class, userId);
        if (likesCount == null || likesCount == 0) {
            return Collections.emptyList();
        }

        return jdbc.query(GET_RECOMMENDATIONS, mapper, userId, userId, userId);
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return jdbc.query(GET_COMMON_FILMS, mapper, userId, friendId);
    }
}
