package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class InMemoryMpaStorage implements MpaStorage {
    private final Map<Long, Mpa> mpa = new HashMap<>();

    public Collection<Mpa> findAllMpa() {
        return mpa.values();
    }

    public Optional<Mpa> findMpaById(Long id) {
        return Optional.ofNullable(mpa.get(id));
    }
}
