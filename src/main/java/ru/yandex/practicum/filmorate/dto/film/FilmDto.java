package ru.yandex.practicum.filmorate.dto.film;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.yandex.practicum.filmorate.dto.mpa.MpaDto;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class FilmDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Long id;
    String name;
    String description;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    LocalDate releaseDate;
    Long duration;
    Set<Long> likes = new HashSet<>();
    LinkedHashSet<Genre> genres = new LinkedHashSet<>();
    MpaDto mpa;
}
