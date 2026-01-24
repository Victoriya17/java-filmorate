package ru.yandex.practicum.filmorate.dto.film;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateFilmRequest {
    @NotNull
    Long id;
    @NotBlank(message = "Название не может быть пустым")
    String name;
    @NotBlank
    @Size(max = 200, message = "Описание не более 200 символов")
    String description;
    @PastOrPresent(message = "Дата не может быть в будущем")
    LocalDate releaseDate;
    @Positive(message = "Длительность должна быть положительной")
    Integer duration;

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
