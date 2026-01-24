package ru.yandex.practicum.filmorate.dto.mpa;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewMpaRequest {
    @NotNull
    String name;
    String description;
}
