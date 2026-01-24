package ru.yandex.practicum.filmorate.dto.genre;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateGenreRequest {
    Long id;
    @NotNull
    String name;
}
