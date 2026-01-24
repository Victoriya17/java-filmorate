package ru.yandex.practicum.filmorate.dto.user;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NewUserRequest {
    @NotBlank
    @Email
    String email;
    @NotBlank
    String login;
    String name;
    @NotNull
    @Past
    LocalDate birthday;
}
