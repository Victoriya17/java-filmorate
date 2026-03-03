package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.director.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.director.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DirectorService {

    private final DirectorStorage directorStorage;

    public DirectorService(@Qualifier("directorDbStorage")DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public DirectorDto create(NewDirectorRequest request) {
        log.debug("Создаем запись о режиссере");

        Director director = DirectorMapper.mapToDirector(request);
        director = directorStorage.create(director);

        return DirectorMapper.mapToDirectorDto(director);
    }

    public DirectorDto update(UpdateDirectorRequest request) {
        log.debug("Обновляем данные о режиссерах");

        if (request.getId() == null) {
            throw new ValidationException("Id режиссера должен быть указан");
        }

        Director director = directorStorage.getById(request.getId())
                .orElseThrow(() -> new NotFoundException("Режиссёр с ID " + request.getId() + "не найден"));

        Director updatedDirector = DirectorMapper.updateDirector(director, request);
        updatedDirector = directorStorage.update(updatedDirector);

        return DirectorMapper.mapToDirectorDto(updatedDirector);
    }

    public DirectorDto getById(Long id) {
        Director director = directorStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с ID " + id + " не найден"));
        return DirectorMapper.mapToDirectorDto(director);
    }

    public List<DirectorDto> getAll() {
        return directorStorage.getAll().stream().map(DirectorMapper::mapToDirectorDto).collect(Collectors.toList());
    }

    public boolean deleteById(Long id) {
        log.debug("Удаляем режиссёра с ID: {}", id);
        return directorStorage.deleteById(id);
    }
}

