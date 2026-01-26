package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface GenreStorage {
    Collection<Genre> findAllGenres();

    Optional<Genre> findGenreById(Long id);

    Optional<Set<Genre>> findGenresByFilmId(Long filmId);

    Set<Long> findExistingGenreIds(Set<Long> genreIds);

    Map<Long, Set<Genre>> findGenresByFilmIds(Collection<Long> filmIds);
}
