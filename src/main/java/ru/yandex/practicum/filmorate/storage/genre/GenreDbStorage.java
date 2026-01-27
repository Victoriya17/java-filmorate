package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.*;
import java.util.stream.Collectors;

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
        List<Genre> genres = jdbc.query(FIND_GENRES_BY_FILM_ID, new GenreRowMapper(), filmId);

        if (genres.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new LinkedHashSet<>(genres));
    }

    @Override
    public Set<Long> findExistingGenreIds(Set<Long> genreIds) {
        if (genreIds.isEmpty()) {
            return Collections.emptySet();
        }

        String sql = "SELECT genre_id FROM genres WHERE genre_id IN (";
        sql += genreIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        sql += ")";

        return jdbc.queryForList(sql, genreIds.toArray(), Long.class)
                .stream()
                .collect(Collectors.toSet());
    }

    @Override
    public Map<Long, Set<Genre>> findGenresByFilmIds(Collection<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = String.join(", ", Collections.nCopies(filmIds.size(), "?"));

        String sql = String.format("""
                SELECT fg.film_id, g.genre_id, g.name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.genre_id
                WHERE fg.film_id IN (%s)
                """, placeholders);

        return jdbc.query(sql, (rs, rowNum) -> {
                    Long filmId = rs.getLong("film_id");
                    Genre genre = new Genre(rs.getLong("genre_id"), rs.getString("name"));
                    return Map.entry(filmId, genre);
                }, filmIds.toArray())
                .stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
                ));
    }
}
