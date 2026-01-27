package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryGenreStorage implements GenreStorage {
    private final Map<Long, Genre> genres = new HashMap<>();
    private final Map<Long, Set<Long>> filmGenres = new HashMap<>();

    public Collection<Genre> findAllGenres() {
        return genres.values();
    }

    public Optional<Genre> findGenreById(Long id) {
        return Optional.ofNullable(genres.get(id));
    }

    public Optional<Set<Genre>> findGenresByFilmId(Long filmId) {
        if (filmId == null) {
            return Optional.empty();
        }

        Set<Long> genreIds = filmGenres.get(filmId);
        if (genreIds == null || genreIds.isEmpty()) {
            return Optional.empty();
        }

        Set<Genre> genres = genreIds.stream()
                .map(this.genres::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return genres.isEmpty()
                ? Optional.empty()
                : Optional.of(genres);
    }

    public Set<Long> findExistingGenreIds(Set<Long> genreIds) {
        if (genreIds.isEmpty()) {
            return Collections.emptySet();
        }

        return genreIds.stream()
                .filter(id -> genres.containsKey(id))
                .collect(Collectors.toSet());
    }

    @Override
    public Map<Long, Set<Genre>> findGenresByFilmIds(Collection<Long> filmIds) {
        return filmIds.stream()
                .filter(filmGenres::containsKey)
                .collect(Collectors.toMap(
                        filmId -> filmId,
                        filmId -> filmGenres.get(filmId).stream()
                                .map(genres::get)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet())
                ));
    }
}
