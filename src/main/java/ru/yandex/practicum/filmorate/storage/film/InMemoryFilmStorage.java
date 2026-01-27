package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final Map<Long, Film> films = new HashMap<>();

    public InMemoryFilmStorage(@Qualifier("inMemoryGenreStorage") GenreStorage genreStorage,
                               @Qualifier("inMemoryMpaStorage")MpaStorage mpaStorage) {
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    public Collection<Film> findAllFilms() {
        return films.values();
    }

    public Film createFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    public Film updateFilm(Film updatedFilm) {
        films.put(updatedFilm.getId(), updatedFilm);
        return updatedFilm;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public Optional<Film> findFilmById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void addFilmGenres(Long filmId, Collection<Long> genreIds) {
        Film film = films.get(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }

        if (film.getGenres() == null) {
            film.setGenres(new LinkedHashSet<>());
        }

        for (Long genreId : genreIds) {
            if (genreId <= 0) {
                throw new ValidationException("ID жанра должен быть положительным числом");
            }

            Optional<Genre> genreOpt = genreStorage.findGenreById(genreId);
            if (!genreOpt.isPresent()) {
                throw new NotFoundException("Жанр с ID " + genreId + " не найден");
            }

            Genre genre = genreOpt.get();

            film.getGenres().add(genre);
        }

        log.debug("К фильму ID={} добавлены жанры: {}", filmId, genreIds);
    }

    @Override
    public Set<Long> findAllLikes(Film film) {
        Set<Long> likes = film.getLikes();

        if (likes == null || likes.isEmpty()) {
            log.trace(String.format("У фильма %s нет лайков", film.getName()));
            return new HashSet<>();
        }

        return likes;
    }

    @Override
    public void addLike(Long id, Long userId) {
        Film film = findFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));

        if (findAllLikes(film).contains(userId)) {
            throw new DuplicatedDataException(String.format("Пользователь %s уже ставил лайк фильму %s",
                    userId, film.getName()));
        }

        findAllLikes(film).add(userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        Film film = findFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));
        if (!findAllLikes(film).contains(userId)) {
            throw new NotFoundException(String.format("Пользователь %s не ставил лайк фильму %s",
                    userId, film.getName()));
        }

        findAllLikes(film).remove(userId);
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count должен быть > 0");
        }

        return findAllFilms()
                .stream()
                .sorted(Comparator.comparing((Film film) -> film.getLikes().size(), Comparator.reverseOrder()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
