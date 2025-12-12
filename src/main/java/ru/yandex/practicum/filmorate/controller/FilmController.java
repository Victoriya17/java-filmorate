package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    @GetMapping
    public Collection<Film> findAllFilms() {
        return films.values();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        if (film == null) {
            throw new ValidationException("Фильм не может быть null");
        }

        validateUniqueFilm(film, false);
        validateDescription(film);
        validateReleaseDate(film);
        validateDuration(film);

        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    private void validateDescription(Film film) {
        if (film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException("Описание фильма не может быть пустым или содержать больше 200 символов.");
        }
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE) ||
                film.getReleaseDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата создания не может быть раньше 12.12.1895 года или быть равна null.");
        }
    }

    private void validateDuration(Film film) {
        if (film.getDuration() < 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void validateUniqueFilm(Film film, boolean isUpdate) {
        if (isUpdate) {
            Film existingFilm = films.get(film.getId());
            if (existingFilm != null &&
                    existingFilm.getName().equals(film.getName()) &&
                    existingFilm.getReleaseDate().equals(film.getReleaseDate())) {
                return;
            }
        }

        for (Film existing : films.values()) {
            if (isUpdate && existing.getId().equals(film.getId())) {
                continue;
            }

            if (existing.getName().equals(film.getName()) &&
                    existing.getReleaseDate().equals(film.getReleaseDate())) {
                throw new DuplicatedDataException("Этот фильм уже есть в списке.");
            }
        }
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        log.info("Начало обновления фильма. ID: {}", newFilm.getId());

        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        Film oldFilm = films.get(newFilm.getId());
        if (oldFilm == null) {
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }

        validateDescription(newFilm);
        validateReleaseDate(newFilm);
        validateDuration(newFilm);
        validateUniqueFilm(newFilm, true);

        oldFilm.setName(newFilm.getName());
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
        oldFilm.setDuration(newFilm.getDuration());

        return oldFilm;
    }
}
