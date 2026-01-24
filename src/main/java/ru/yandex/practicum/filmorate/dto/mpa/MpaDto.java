package ru.yandex.practicum.filmorate.dto.mpa;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MpaDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Long id;
    @NotNull
    String name;
}
