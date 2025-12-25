package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    public Collection<Film> findAllFilms() {
        log.debug("Получение списка фильмов");
        return filmStorage.findAllFilms();
    }

    public Film createFilm(Film film) {
        log.debug("Запись фильма {}", film.getName());
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        log.debug("Обновление фильма {}", film.getName());
        return filmStorage.updateFilm(film);
    }

    public Film findFilmById(Long id) {
        log.debug("Поиск фильма по ID {}", id);
        return filmStorage.findFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    public void addLike(Long id, Long userId) {
        userService.findUserById(userId);
        Film film = findFilmById(id);

        film.getLikes().add(userId);

        log.info("Пользователь ID={} поставил лайк фильму ID={}", userId, id);
    }

    public void removeLike(Long filmId, Long userId) {
        userService.findUserById(userId);
        Film film = findFilmById(filmId);

        film.getLikes().remove(userId);

        log.info("Пользователь ID={} убрал лайк фильму ID={}", userId, filmId);
    }

    public Collection<Film> getPopularFilms(int count) {
        log.debug("Получаем список из первых {} фильмов по количеству лайков", count);

        return findAllFilms().stream()
                .sorted(Comparator.comparing(
                        film -> film.getLikes().size(),
                        Comparator.reverseOrder()
                ))
                .limit(count)
                .collect(Collectors.toList());
    }
}
