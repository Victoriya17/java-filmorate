package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    public Collection<Film> findAllFilms() {
        return films.values();
    }

    public Film createFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) { // если оставить @NotBlank не проходил тест в Postman
            throw new ValidationException("Название фильма не может быть пустым");
        }

        validateUniqueOnCreate(film);
        validateDescription(film.getDescription());
        validateReleaseDate(film.getReleaseDate());
        validateDuration(film.getDuration());

        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    private void validateDescription(String description) {
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException("Описание фильма не может быть пустым или содержать больше 200 символов.");
        }
    }

    private void validateReleaseDate(LocalDate date) {
        if (date.isBefore(FIRST_FILM_DATE) || date.isAfter(LocalDate.now())) {
            throw new ValidationException("Дата создания не может быть раньше 12.12.1895 года или быть равна null.");
        }
    }

    private void validateDuration(int duration) {
        if (duration < 0) {
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

    private void validateUniqueOnCreate(Film film) {
        for (Film existing : films.values()) {
            if (existing.getName().equals(film.getName()) &&
                    existing.getReleaseDate().equals(film.getReleaseDate())) {
                throw new DuplicatedDataException("Этот фильм уже есть в списке.");
            }
        }
    }

    public Film updateFilm(Film newFilm) {
        log.info("Начало обновления фильма. ID: {}", newFilm.getId());

        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        Film oldFilm = films.get(newFilm.getId());
        if (oldFilm == null) {
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }

        validateUniqueOnUpdate(
                (newFilm.getName() != null) ? newFilm.getName() : oldFilm.getName(),
                (newFilm.getReleaseDate() != null) ? newFilm.getReleaseDate() : oldFilm.getReleaseDate(),
                newFilm.getId()
        );

        validateNameAndDescriptionOnUpdate(newFilm, oldFilm);
        validateReleaseDateAndDurationOnUpdate(newFilm, oldFilm);

        return oldFilm;
    }

    private void validateUniqueOnUpdate(String name, LocalDate releaseDate, Long filmId) {
        for (Film existing : films.values()) {
            if (existing.getId().equals(filmId)) {
                continue;
            }
            if (existing.getName().equals(name) && existing.getReleaseDate().equals(releaseDate)) {
                throw new DuplicatedDataException("Этот фильм уже есть в списке.");
            }
        }
    }

    private void validateNameAndDescriptionOnUpdate(Film newFilm, Film oldFilm) {
        if (newFilm.getName() != null && !newFilm.getName().equals(oldFilm.getName())) {
            oldFilm.setName(newFilm.getName());
        }
        if (newFilm.getDescription() != null && !newFilm.getDescription().isBlank() &&
                !newFilm.getDescription().equals(oldFilm.getDescription())) {
            validateDescription(newFilm.getDescription());
            oldFilm.setDescription(newFilm.getDescription());
        }
    }

    private void validateReleaseDateAndDurationOnUpdate(Film newFilm, Film oldFilm) {
        if (newFilm.getReleaseDate() != null && !newFilm.getReleaseDate().equals(oldFilm.getReleaseDate())) {
            validateReleaseDate(newFilm.getReleaseDate());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() != null && !newFilm.getDuration().equals(oldFilm.getDuration())) {
            validateDuration(newFilm.getDuration());
            oldFilm.setDuration(newFilm.getDuration());
        }
    }

    public Film findFilmById(Long id) {
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        return film;
    }
}
