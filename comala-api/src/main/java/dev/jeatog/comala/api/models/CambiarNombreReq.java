package dev.jeatog.comala.api.models;

import jakarta.validation.constraints.NotBlank;

public record CambiarNombreReq(
        @NotBlank String nombre
) {}
