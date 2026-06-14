package ru.yandex.practicum.filmorate.dto.film;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Director;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class UpdateFilmRequest {
    @NotNull
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Set<Director> directors = new HashSet<>();

    public boolean hasDescription() {
        return !(description == null || description.isBlank());
    }

    public boolean hasReleaseDate() {
        return !(releaseDate == null);
    }

    public boolean hasDuration() {
        return !(duration == null);
    }

    public boolean hasDirectors() {
        return !(directors == null);
    }
}
