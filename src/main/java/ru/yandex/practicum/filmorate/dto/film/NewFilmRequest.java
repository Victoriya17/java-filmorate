package ru.yandex.practicum.filmorate.dto.film;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class NewFilmRequest {
    @NotBlank(message = "Название не может быть пустым")
    private String name;
    @NotBlank
    @Size(max = 200, message = "Описание не более 200 символов")
    private String description;
    @NotNull
    @PastOrPresent(message = "Дата не может быть в будущем или равна нулю")
    private LocalDate releaseDate;
    @Positive(message = "Длительность должна быть положительной")
    private Integer duration;
    private Set<Long> likes = new HashSet<>();
    private LinkedHashSet<Genre> genres = new LinkedHashSet<>();
    private Mpa mpa;
}
