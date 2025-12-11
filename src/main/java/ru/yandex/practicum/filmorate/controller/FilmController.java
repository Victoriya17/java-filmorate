package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);

    @GetMapping
    public Collection<Film> findAllFilms() {
        return films.values();
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        if (film == null) {
            throw new ValidationException("Фильм не может быть null");
        }

        validateName(film);
        validateUniqueFilm(film);
        validateDescription(film);
        validateReleaseDate(film);
        validateDuration(film);

        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    private void validateUniqueFilm(Film film) {
        for (Film existingFilm : films.values()) {

            if (existingFilm.getId().equals(film.getId())) {
                continue;
            }

            if (existingFilm.getName().equals(film.getName()) &&
                    existingFilm.getReleaseDate().equals(film.getReleaseDate())) {
                throw new DuplicatedDataException("Этот фильм уже есть в списке.");
            }
        }
    }

    private void validateName(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым.");
        }
    }

    private void validateDescription(Film film) {
        if (film.getDescription() == null || film.getDescription().isBlank() || film.getDescription().length() > 200) {
            throw new ValidationException("Описание фильма не может быть больше 200 символов.");
        }
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28)) ||
                film.getReleaseDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Фильм не может быть создан раньше 12 декабря 1895 года.");
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

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        log.info("Начало обновления фильма. ID: {}", newFilm.getId());

        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            updateNameIfProvided(newFilm, oldFilm);
            updateDescriptionIfValid(newFilm, oldFilm);
            updateReleaseDateIfValid(newFilm, oldFilm);
            updateDurationIfValid(newFilm, oldFilm);
            validateUniqueFilm(newFilm);

            return oldFilm;
        }
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    private void updateNameIfProvided(Film newFilm, Film oldFilm) {
        if (newFilm.getName() != null && !newFilm.getName().isBlank()) {
            oldFilm.setName(newFilm.getName());
        }
    }

    private void updateDescriptionIfValid(Film newFilm, Film oldFilm) {
        if (newFilm.getDescription() != null && !newFilm.getDescription().isBlank() &&
                newFilm.getDescription().length() <= 200) {
            oldFilm.setDescription(newFilm.getDescription());
        }
    }

    private void updateReleaseDateIfValid(Film newFilm, Film oldFilm) {
        if (newFilm.getReleaseDate() != null &&
                newFilm.getReleaseDate().isAfter(LocalDate.of(1895, 12, 28)) &&
                newFilm.getReleaseDate().isBefore(LocalDate.now())) {
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }
    }

    private void updateDurationIfValid(Film newFilm, Film oldFilm) {
        if (newFilm.getDuration() > 0) {
            oldFilm.setDuration(newFilm.getDuration());
        }
    }
}
