package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private Film film;
    private Film existingFilm;
    private Film updatedFilm;
    private FilmController filmController;

    @BeforeEach
    void beforeEach() {
        film = new Film();
        filmController = new FilmController();
    }

    @Test
    void shouldReturnAllFilmsFromStorage() {
        film.setName("Chocolate");
        film.setDescription("Film about confectioner");
        film.setReleaseDate(LocalDate.of(2000, 12, 22));
        film.setDuration(121);

        Film film2 = new Film();
        film2.setName("Intouchables");
        film2.setDescription("Film about friendship");
        film2.setReleaseDate(LocalDate.of(2012, 4, 26));
        film2.setDuration(114);

        filmController.createFilm(film);
        filmController.createFilm(film2);


        Collection<Film> allFilms = filmController.findAllFilms();

        assertNotNull(allFilms, "Коллекция не должна быть null");
        assertEquals(2, allFilms.size(), "Должно вернуться 2 фильма");
    }

    @Test
    void shouldReturnEmptyCollectionWhenNoFilms() {
        Collection<Film> allFilms = filmController.findAllFilms();

        assertNotNull(allFilms, "Коллекция не должна быть null при пустом хранилище");
        assertTrue(allFilms.isEmpty(), "При отсутствии фильмов коллекция должна быть пустой");
        assertEquals(0, allFilms.size(), "Размер коллекции должен быть 0");
    }

    @Test
    void testDuplicatedFilm() {
        film.setName("Chocolate");
        film.setDescription("Film about confectioner");
        film.setReleaseDate(LocalDate.of(2000, 12, 22));
        film.setDuration(121);

        filmController.createFilm(film);

        Film film2 = new Film();
        film2.setName("Chocolate");
        film2.setDescription("Other");
        film2.setReleaseDate(LocalDate.of(2000, 12, 22));
        film2.setDuration(100);

        assertThrows(DuplicatedDataException.class, () -> {
            filmController.createFilm(film2);
        });
        assertEquals(1, filmController.findAllFilms().stream()
                .filter(u -> "Chocolate".equals(u.getName()))
                .count(), "");
        assertEquals(1, filmController.findAllFilms().stream()
                .filter(u -> LocalDate.of(2000, 12, 22).equals(u.getReleaseDate()))
                .count(), "");
    }

    @Test
    void testCreateNameNull() {
        film.setName(null);
        film.setDescription("Film about confectioner");
        film.setReleaseDate(LocalDate.of(2000, 12, 22));
        film.setDuration(121);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    @Test
    void testCreateNameIsBlank() {
        film.setName("");
        film.setDescription("Film about confectioner");
        film.setReleaseDate(LocalDate.of(2000, 12, 22));
        film.setDuration(121);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    private void assertDescriptionThrowsValidationException(String description) {
        film.setName("Chocolate");
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2000, 12, 22));
        film.setDuration(121);
        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    @Test
    void testDescriptionLessTwoHundredSymbols() {
        assertDescriptionThrowsValidationException("Chocolat is about a mysterious woman, Vianne Rocher, and her " +
                "daughter who open a magical chocolate shop in a small, rigid French village during Lent, bringing " +
                "temptation and challenging traditions with her unique, desire-fulfilling chocolates, especially as " +
                "the conservative mayor opposes her, until a group of river gypsies and Vianne's own past bring " +
                "about change and self-discovery, with themes of indulgence, tradition, love, and finding oneself.");
    }

    @Test
    void testDescriptionNull() {
       assertDescriptionThrowsValidationException(null);
    }

    @Test
    void testDescriptionIsBlank() {
        assertDescriptionThrowsValidationException("");
    }

    private void assertReleaseDateThrowsValidationException(LocalDate date) {
        film.setName("Chocolate");
        film.setDescription("Film about confectioner");
        film.setReleaseDate(date);
        film.setDuration(121);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    @Test
    void testCreateWithReleaseDateNull() {
        assertReleaseDateThrowsValidationException(null);
    }

    @Test
    void testCreateWithReleaseDateNotInFuture() {
        assertReleaseDateThrowsValidationException(LocalDate.of(2030, 12, 22));
    }

    @Test
    void testCreateWithReleaseDateNotInPast() {
        assertReleaseDateThrowsValidationException(LocalDate.of(1890, 12, 28));
    }

    @Test
    void testCreateWithDurationNegative() {
        film.setName("Chocolate");
        film.setDescription("Film about confectioner");
        film.setReleaseDate(LocalDate.of(2000, 12, 22));
        film.setDuration(-121);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    private void beforeUpdateFilmTest() {
        existingFilm = new Film();
        existingFilm.setId(2L);
        existingFilm.setName("Intouchables");
        existingFilm.setDescription("Film about friendship");
        existingFilm.setReleaseDate(LocalDate.of(2012, 4, 26));
        existingFilm.setDuration(114);

        filmController.createFilm(existingFilm);

        updatedFilm = new Film();
        updatedFilm.setId(existingFilm.getId());
        updatedFilm.setName("1+1");
    }

    @Test
    void shouldUpdateAllFieldsSuccessfully() {
        beforeUpdateFilmTest();
        updatedFilm.setDescription("Film about friendship with two not similar people");
        updatedFilm.setDuration(115);
        updatedFilm.setReleaseDate(LocalDate.of(1995, 5, 15));

        Film result = filmController.updateFilm(updatedFilm);

        assertEquals("1+1", result.getName());
        assertEquals("Film about friendship with two not similar people", result.getDescription());
        assertEquals(LocalDate.of(1995, 5, 15), result.getReleaseDate());
        assertEquals(115, result.getDuration());
    }

    @Test
    void shouldRejectNameNull() {
        beforeUpdateFilmTest();
        updatedFilm.setName(null);
        updatedFilm.setDescription("Film about friendship.");
        updatedFilm.setDuration(115);
        updatedFilm.setReleaseDate(LocalDate.of(1995, 5, 15));

        assertThrows(ValidationException.class, () -> filmController.updateFilm(film));
    }

    @Test
    void shouldRejectNameIsBlank() {
        beforeUpdateFilmTest();
        updatedFilm.setName("");
        updatedFilm.setDescription("Film about friendship.");
        updatedFilm.setDuration(115);
        updatedFilm.setReleaseDate(LocalDate.of(1995, 5, 15));

        assertThrows(ValidationException.class, () -> filmController.updateFilm(film));
    }

    private void assertUpdateDescriptionThrowsValidationException(String description) {
        beforeUpdateFilmTest();
        updatedFilm.setDescription(description);
        updatedFilm.setReleaseDate(LocalDate.of(1995, 5, 15));
        updatedFilm.setDuration(115);
        assertThrows(ValidationException.class, () -> filmController.updateFilm(film));
    }

    @Test
    void shouldRejectDescriptionNull() {
    assertUpdateDescriptionThrowsValidationException(null);
    }

    @Test
    void shouldRejectDescriptionIsBlank() {
       assertUpdateDescriptionThrowsValidationException("");
    }

    @Test
    void shouldRejectDescriptionMoreTwoHundred() {
        assertUpdateDescriptionThrowsValidationException("The French film \"The Intouchables,\" is a heartwarming " +
                "comedy-drama about an unlikely friendship between a wealthy, paralyzed aristocrat named Philippe " +
                "and his new caregiver, Driss, a young man from the projects with a criminal record, who applies for " +
                "the job just to get rejected, but ends up bringing joy and new perspectives to both their lives.");
    }

    @Test
    void shouldRejectReleaseDateInFuture() {
        beforeUpdateFilmTest();
        updatedFilm.setDescription("Film about friendship with two not similar people");
        updatedFilm.setDuration(115);
        updatedFilm.setReleaseDate(LocalDate.of(2030, 5, 15));

        assertEquals(LocalDate.of(2012, 4, 26),
                filmController.updateFilm(updatedFilm).getReleaseDate());
    }

    @Test
    void shouldRejectReleaseDateNull() {
        beforeUpdateFilmTest();
        updatedFilm.setDescription("Film about friendship with two not similar people");
        updatedFilm.setDuration(115);
        updatedFilm.setReleaseDate(null);

        assertEquals(LocalDate.of(2012, 4, 26),
                filmController.updateFilm(updatedFilm).getReleaseDate());
    }

    @Test
    void shouldRejectReleaseDateInPast() {
        beforeUpdateFilmTest();
        updatedFilm.setDescription("Film about friendship with two not similar people");
        updatedFilm.setDuration(115);
        updatedFilm.setReleaseDate(LocalDate.of(1890, 5, 15));

        Film result = filmController.updateFilm(updatedFilm);

        assertEquals(LocalDate.of(2012, 4, 26), result.getReleaseDate());
    }

    @Test
    void shouldRejectDurationInvalid() {
        beforeUpdateFilmTest();
        updatedFilm.setDescription("Film about friendship with two not similar people");
        updatedFilm.setDuration(-114);
        updatedFilm.setReleaseDate(LocalDate.of(1995, 5, 15));

        Film result = filmController.updateFilm(updatedFilm);

        assertEquals(114, result.getDuration());
    }
}
