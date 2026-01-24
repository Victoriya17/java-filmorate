package ru.yandex.practicum.filmorate.storage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@ComponentScan("ru.yandex.practicum.filmorate")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final GenreDbStorage genreStorage;

    @Test
    public void testFindAllFilms() {
        assertThat(filmStorage.findAllFilms()).isNotEmpty()
                .hasSize(4)
                .filteredOn("name", "1+1")
                .isNotEmpty()
                .hasExactlyElementsOfTypes(Film.class);
    }

    @Test
    public void testCreateFilm() {
        Film newFilm = new Film();
        newFilm.setName("Престиж");
        newFilm.setDescription("Роберт и Альфред — фокусники-иллюзионисты, которые на рубеже XIX и XX веков " +
                "соперничали друг с другом ...");
        newFilm.setReleaseDate(LocalDate.of(2006, 10, 17));
        newFilm.setDuration(130);
        newFilm.setMpa(new Mpa());

        assertThat(filmStorage.createFilm(newFilm))
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 5L);
    }

    @Test
    public void testUpdateFilm() {
        Film film = new Film();
        film.setName("Шоколад");
        film.setDescription("Фильм о жизни кондитера");
        film.setReleaseDate(LocalDate.of(2000, 12, 22));
        film.setDuration(130);
        film.setMpa(new Mpa());
        film.setId(2L);

        assertThat(filmStorage.updateFilm(film))
                .isNotNull()
                .hasFieldOrPropertyWithValue("duration", 130);
    }

    @Test
    public void testFindUserById() {
        Optional<Film> filmOptional = filmStorage.findFilmById(1L);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", 1L)
                                .hasFieldOrPropertyWithValue("name", "1+1")
                );
    }

    @Test
    public void testAddFilmGenres() {
        Film film = new Film();
        film.setName("Шоколад");
        film.setDescription("Фильм о жизни кондитера");
        film.setReleaseDate(LocalDate.of(2000, 12, 22));

        Mpa mpa = new Mpa();
        mpa.setId(3L);
        mpa.setName("PG-13");
        film.setMpa(mpa);

        Film savedFilm = filmStorage.createFilm(film);

        List<Long> genreIds = List.of(1L, 2L);

        filmStorage.addFilmGenres(savedFilm.getId(), genreIds);

        Optional<Set<Genre>> result = genreStorage.findGenresByFilmId(savedFilm.getId());

        assertThat(result)
                .isPresent()
                .hasValueSatisfying(genres -> {
                    assertThat(genres).hasSize(2);

                    assertThat(genres).anySatisfy(genre -> {
                        assertThat(genre.getId()).isEqualTo(1L);
                        assertThat(genre.getName()).isEqualTo("Комедия");
                    });
                    assertThat(genres).anySatisfy(genre -> {
                        assertThat(genre.getId()).isEqualTo(2L);
                        assertThat(genre.getName()).isEqualTo("Драма");
                    });
                });
    }

    @Test
    public void testAddLikes() {
        Film film = new Film();
        film.setId(3L);
        film.setName("Зверополис");

        User user = new User();
        user.setId(1L);
        user.setEmail("green@yandex.ru");
        user.setName("Green");

        assertThat(filmStorage.findAllLikes(film))
                .isNotEmpty()
                .isInstanceOf(Set.class)
                .hasSize(2)
                .containsOnly(2L, 3L);

        filmStorage.addLike(film.getId(), user.getId());

        assertThat(filmStorage.findAllLikes(film))
                .isNotEmpty()
                .isInstanceOf(Set.class)
                .hasSize(3)
                .containsOnly(1L, 2L, 3L);
    }

    @Test
    public void testFindAllLikes() {
        Film film = new Film();
        film.setId(2L);
        film.setName("Шоколад");
        film.setDescription("Фильм о жизни кондитера");

        assertThat(filmStorage.findAllLikes(film)).isNotEmpty()
                .isInstanceOf(Set.class)
                .hasSize(3)
                .containsOnly(1L, 2L, 3L);
    }

    @Test
    public void testRemoveLike() {
        Film film = new Film();
        film.setId(3L);
        film.setName("Зверополис");

        User user = new User();
        user.setId(2L);
        user.setEmail("yellow@yandex.ru");
        user.setName("Yellow");

        filmStorage.removeLike(film.getId(), user.getId());

        assertThat(filmStorage.findAllLikes(film))
                .isNotEmpty()
                .isInstanceOf(Set.class)
                .hasSize(1)
                .containsOnly(3L);
    }

    @Test
    public void testGetPopularFilm() {
        final int count = 4;
        Collection<Film> films = filmStorage.getPopularFilms(count);

        assertThat(films).isNotEmpty()
                .hasSize(count)
                .isInstanceOf(Collection.class)
                .first()
                .extracting(Film::getId)
                .isEqualTo(2L);
    }
}
