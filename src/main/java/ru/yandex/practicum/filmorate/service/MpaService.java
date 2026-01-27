package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.mpa.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MpaService {
    private final MpaStorage mpaStorage;

    public MpaService(@Qualifier("mpaDbStorage")MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Collection<MpaDto> findAllMpa() {
        log.debug("Получение списка рейтингов фильмов");
        return mpaStorage.findAllMpa().stream().map(MpaMapper::mapToMpaDto).collect(Collectors.toList());
    }

    public MpaDto findMpaById(Long id) {
        log.debug("Поиск рейтинга фильма по ID {}", id);
        Mpa mpa = mpaStorage.findMpaById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id " + id + " не найден"));
        return MpaMapper.mapToMpaDto(mpa);
    }
}
