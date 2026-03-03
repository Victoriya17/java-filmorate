package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.director.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.director.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DirectorDto create(@Valid @RequestBody NewDirectorRequest director) {
        log.info("Получен POST /directors: {}", director);
        return directorService.create(director);
    }

    @PutMapping
    public DirectorDto update(@Valid @RequestBody UpdateDirectorRequest director) {
        return directorService.update(director);
    }

    @GetMapping("/{id}")
    public DirectorDto getById(@PathVariable Long id) {
        log.info("Получен GET /directors/{}", id);
        return directorService.getById(id);

    }

    @GetMapping
    public List<DirectorDto> getAll() {
        log.info("Получен GET /directors");
        List<DirectorDto> result = directorService.getAll();
        log.info("Отдан ответ GET /directors: {}", result);
        return result;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public boolean deleteById(@PathVariable Long id) {
        return directorService.deleteById(id);
    }
}
