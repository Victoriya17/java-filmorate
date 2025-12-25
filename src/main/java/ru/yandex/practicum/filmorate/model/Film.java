package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Long id;
    private String name;
    @NotBlank
    private String description;
    @NotNull
    private LocalDate releaseDate;
    private Integer duration;
    private Set<Long> likes = new HashSet<>();
}
