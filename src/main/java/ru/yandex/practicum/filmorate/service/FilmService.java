package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public void addLike(Long id, Long userId) {
        if (id == null) {
            throw new ValidationException("Id фильма должен быть указан");
        }
        if (userId == null) {
            throw new ValidationException("Id пользователя должен быть указан");
        }

        userStorage.findUserById(userId);
        Film film = filmStorage.findFilmById(id);

        film.getLikes().add(userId);
        filmStorage.updateFilm(film);

        log.info("Пользователь ID={} поставил лайк фильму ID={}", userId, id);
    }

    public void removeLike(Long filmId, Long userId) {
        if (userId == null) {
            throw new ValidationException("Id пользователя должен быть указан");
        }
        if (filmId == null) {
            throw new ValidationException("Id фильма должен быть указан");
        }
        userStorage.findUserById(userId);
        Film film = filmStorage.findFilmById(filmId);

        film.getLikes().remove(userId);
        filmStorage.updateFilm(film);

        log.info("Пользователь ID={} убрал лайк фильму ID={}", userId, filmId);
    }

    public Collection<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Количество фильмов должно быть больше 0");
        }
        log.debug("Получаем список из первых {} фильмов по количеству лайков", count);

        return filmStorage.findAllFilms().stream()
                .sorted(Comparator.comparing(
                        film -> film.getLikes().size(),
                        Comparator.reverseOrder()
                ))
                .limit(count)
                .collect(Collectors.toList());
    }
}
