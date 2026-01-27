package ru.yandex.practicum.filmorate.storage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@ComponentScan("ru.yandex.practicum.filmorate")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaDbStorageTest {
    MpaDbStorage mpaStorage;

    @Test
    public void testFindMpaById() {
        assertThat(mpaStorage.findMpaById(1L))
                .isPresent()
                .hasValueSatisfying(mpa ->
                        assertThat(mpa).hasFieldOrPropertyWithValue("id", 1L)
                                .hasFieldOrPropertyWithValue("name", "G")
                );
    }

    @Test
    public void testFindMpaByIdNotFound() {
        assertThat(mpaStorage.findMpaById(999L))
                .isEmpty();
    }

    @Test
    public void testFindAllMpa() {
        assertThat(mpaStorage.findAllMpa()).isNotEmpty()
                .hasSize(5)
                .filteredOn("name", "PG-13")
                .isNotEmpty()
                .hasExactlyElementsOfTypes(Mpa.class);
    }
}
