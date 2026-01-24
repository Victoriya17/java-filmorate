package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("genreDbStorage") GenreStorage genreStorage,
                       @Qualifier("mpaDbStorage") MpaStorage mpaStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    public Collection<FilmDto> findAllFilms() {
        log.debug("Получение списка фильмов");
        return filmStorage.findAllFilms()
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public FilmDto createFilm(NewFilmRequest request) {
        log.debug("Запись фильма {}", request.getName());
        validateReleaseDate(request.getReleaseDate());

        Film film = FilmMapper.mapToFilm(request);

        if (request.getMpa() != null) {
            film = createFilmWithMpa(film);
        }

        Film createdFilm = filmStorage.createFilm(film);

        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            addGenresToFilm(createdFilm, request.getGenres());
        }

        log.trace("Фильм '{}' успешно создан", createdFilm.getName());
        return FilmMapper.mapToFilmDto(createdFilm);
    }

    private void validateReleaseDate(LocalDate date) {
        if (date.isBefore(FIRST_FILM_DATE)) {
            throw new ValidationException("Дата создания не может быть раньше 12.12.1895 года или быть равна null.");
        }
    }

    private Film createFilmWithMpa(Film film) {
        if (film.getMpa() == null) {
            return film;
        }

        Long mpaId = film.getMpa().getId();
        if (mpaId == null || mpaId <= 0) {
            throw new ValidationException("Рейтинг должен быть положительным числом");
        }

        Mpa mpa = mpaStorage.findMpaById(mpaId)
                .orElseThrow(() -> new NotFoundException("Рейтинг с ID " + mpaId + " не найден"));
        film.setMpa(mpa);
        return film;
    }

    private void addGenresToFilm(Film film, Set<Genre> genres) {
        if (film.getId() == null) {
            throw new ValidationException("Фильм не сохранён в БД (ID = null). Невозможно добавить жанры.");
        }

        Set<Long> genreIds = genres.stream()
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .collect(Collectors.toSet());

        Set<Long> validGenreIds = validateGenreIds(genreIds);


        if (!validGenreIds.isEmpty()) {
            filmStorage.addFilmGenres(film.getId(), validGenreIds);
        }
    }

    private Set<Long> validateGenreIds(Set<Long> genreIds) {
        if (genreIds.isEmpty()) {
            log.warn("В запросе отсутствуют валидные ID жанров");
            return Collections.emptySet();
        }

        Set<Long> existingGenreIds = genreIds.stream()
                .filter(genreId -> genreStorage.findGenreById(genreId).isPresent())
                .collect(Collectors.toSet());

        Set<Long> missingGenreIds = new HashSet<>(genreIds);
        missingGenreIds.removeAll(existingGenreIds);

        if (!missingGenreIds.isEmpty()) {
            log.warn("Пропущены несуществующие жанры с ID: {}", missingGenreIds);
        }

        if (existingGenreIds.isEmpty()) {
            throw new NotFoundException(
                    ("Все указанные жанры не найдены в БД. IDs: " + missingGenreIds));
        }

        return existingGenreIds;
    }

    public FilmDto updateFilm(UpdateFilmRequest request) {
        log.debug("Обновление фильма {}", request.getName());

        if (request.getId() == null) {
            throw new ValidationException("ID фильма не может быть null");
        }

        Film film = filmStorage.findFilmById(request.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + request.getId() + "не найден"));

        Film updatedFilm = FilmMapper.updateFilmFields(film, request);

        if (updatedFilm.getReleaseDate() != null) {
            validateReleaseDate(updatedFilm.getReleaseDate());
        }

        updatedFilm = filmStorage.updateFilm(updatedFilm);

        return FilmMapper.mapToFilmDto(updatedFilm);
    }

    public FilmDto findFilmById(Long id) {
        log.debug("Поиск фильма по ID {}", id);
        Film film = filmStorage.findFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
        Optional<Set<Genre>> genres = genreStorage.findGenresByFilmId(film.getId());
        film.setGenres((LinkedHashSet<Genre>) genres.orElse(Collections.emptySet()));
        return FilmMapper.mapToFilmDto(film);
    }

    public void addLike(Long id, Long userId) {
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
        filmStorage.findFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));

        filmStorage.addLike(id, userId);

        log.info("Пользователь ID={} поставил лайк фильму ID={}", userId, id);
    }

    public void removeLike(Long filmId, Long userId) {
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        filmStorage.findFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));

        filmStorage.removeLike(filmId, userId);

        log.info("Пользователь ID={} убрал лайк фильму ID={}", userId, filmId);
    }

    public Collection<FilmDto> getPopularFilms(int count) {
        log.debug("Получаем список из первых {} фильмов по количеству лайков", count);
        if (count <= 0) {
            throw new ValidationException("Количество фильмов должно быть больше 0");
        }

        Collection<Film> films = filmStorage.getPopularFilms(count);

        if (films.isEmpty()) {
            log.warn("Не найдено популярных фильмов (запрос: {})", count);
            return Collections.emptyList();
        }

        List<FilmDto> filmDtos = films.stream()
                .map(film -> {
                    FilmDto dto = FilmMapper.mapToFilmDto(film);
                    if (dto.getLikes() == null) {
                        dto.setLikes(new HashSet<>());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Возвращено {} популярных фильмов (запрос: {})", filmDtos.size(), count);
        return filmDtos;
    }
}
