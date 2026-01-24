package ru.yandex.practicum.filmorate.storage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@ComponentScan("ru.yandex.practicum.filmorate")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreStorageTest {
    GenreDbStorage genreStorage;

    @Test
    public void testFindGenreById() {
        assertThat(genreStorage.findGenreById(1L))
                .isPresent()
                .hasValueSatisfying(genre ->
                        assertThat(genre).hasFieldOrPropertyWithValue("id", 1L)
                                .hasFieldOrPropertyWithValue("name", "Комедия")
                );
    }

    @Test
    public void testFindGenreByIdNotFound() {
        assertThat(genreStorage.findGenreById(66L))
                .isEmpty();
    }

    @Test
    public void testFindAllGenre() {
        assertThat(genreStorage.findAllGenres()).isNotEmpty()
                .hasSize(6)
                .filteredOn("name", "Драма")
                .isNotEmpty()
                .hasExactlyElementsOfTypes(Genre.class);
    }

    @Test
    public void testFindGenresByFilmId() {
        Set<Genre> expectedGenres = new LinkedHashSet<>();
        Genre genre = new Genre();
        genre.setId(1L);
        genre.setName("Комедия");
        Genre genre1 = new Genre();
        genre1.setId(2L);
        genre1.setName("Драма");
        expectedGenres.add(genre);
        expectedGenres.add(genre1);

        Optional<Set<Genre>> result = genreStorage.findGenresByFilmId(1L);

        assertThat(result)
                .isPresent()
                .hasValueSatisfying(genres -> {
                    assertThat(genres).hasSize(2);
                    assertThat(genres).containsAll(expectedGenres);
                });
    }
}
