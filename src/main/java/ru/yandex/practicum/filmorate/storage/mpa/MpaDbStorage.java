package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaDbStorage extends BaseDbStorage<Mpa> implements MpaStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM ratings";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM ratings WHERE id = ?";

    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        super(jdbc, mapper, Mpa.class);
    }

    @Override
    public List<Mpa> findAllMpa() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Mpa> findMpaById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }
}
