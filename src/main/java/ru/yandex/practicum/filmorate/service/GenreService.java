package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GenreService {
    private final GenreStorage genreStorage;

    public GenreService(@Qualifier("genreDbStorage") GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Collection<GenreDto> findAllGenres() {
        log.debug("Получение списка жанров фильмов");
        return genreStorage.findAllGenres().stream().map(GenreMapper::mapToGenreDto).collect(Collectors.toList());
    }

    public GenreDto findGenreById(Long id) {
        log.debug("Поиск жанра фильма по ID {}", id);
        Genre genre = genreStorage.findGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id " + id + " не найден"));
        return GenreMapper.mapToGenreDto(genre);
    }
}
