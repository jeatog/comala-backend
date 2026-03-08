package dev.jeatog.comala.api.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record VarianteReq(
        @NotBlank String nombreVariante,
        @NotNull @Positive BigDecimal precioBase
) {}
