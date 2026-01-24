package ru.yandex.practicum.filmorate.dto.mpa;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMpaRequest {
    Long id;
    @NotNull
    String name;
    String description;

    public boolean hasDescription() {
        return ! (description == null || description.isBlank());
    }
}
