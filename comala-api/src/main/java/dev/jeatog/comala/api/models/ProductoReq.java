package dev.jeatog.comala.api.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProductoReq(
        @NotBlank String nombre,
        @NotNull UUID categoriaId
) {}
