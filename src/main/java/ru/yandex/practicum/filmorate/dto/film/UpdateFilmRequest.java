package ru.yandex.practicum.filmorate.dto.film;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateFilmRequest {
    @NotNull
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;

    public boolean hasDescription() {
        return ! (description == null || description.isBlank());
    }

    public boolean hasReleaseDate() {
        return ! (releaseDate == null);
    }

    public boolean hasDuration() {
        return ! (duration == null);
    }
}
