package dev.jeatog.comala.api.models;

import jakarta.validation.constraints.NotBlank;

public record NegocioReq(
        @NotBlank String nombre
) {}
