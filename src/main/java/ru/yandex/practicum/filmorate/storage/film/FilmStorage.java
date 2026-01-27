package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {
    Collection<Film> findAllFilms();

    Film createFilm(Film film);

    Film updateFilm(Film newFilm);

    Optional<Film> findFilmById(Long id);

    void addFilmGenres(Long filmId, Collection<Long> genreIds);

    Set<Long> findAllLikes(Film film);

    void addLike(Long id, Long userId);

    void removeLike(Long filmId, Long userId);

    Collection<Film> getPopularFilms(int count);
}
