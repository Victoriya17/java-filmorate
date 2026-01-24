package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Mpa {
    Long id;
    @NotNull
    String name;
    String description;
}
