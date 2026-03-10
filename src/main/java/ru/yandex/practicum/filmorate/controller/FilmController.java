package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<FilmDto> findAllFilms() {
        return filmService.findAllFilms();
    }

    @PostMapping
    public FilmDto createFilm(@Valid @RequestBody NewFilmRequest film) {
        return filmService.createFilm(film);
    }

    @PutMapping
    public FilmDto updateFilm(@Valid @RequestBody UpdateFilmRequest newFilm) {
        return filmService.updateFilm(newFilm);
    }

    @GetMapping("/{id}")
    public FilmDto findFilmById(@PathVariable Long id) {
        return filmService.findFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<FilmDto> getPopularFilms(@Positive(message = "Количество фильмов должно быть больше 0")
                                               @RequestParam(defaultValue = "10") int count,
                                               @RequestParam(required = false) Long genreId,
                                               @RequestParam(required = false) Integer year) {
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable Long directorId,
                                         @RequestParam(name = "sortBy", defaultValue = "year") String sortBy) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public boolean deleteFilmById(@PathVariable Long id) {
        return filmService.deleteById(id);
    }
}
