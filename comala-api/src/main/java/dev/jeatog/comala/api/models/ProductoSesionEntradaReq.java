package dev.jeatog.comala.api.models;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductoSesionEntradaReq(
        @NotNull UUID varianteId,
        @NotNull @Positive BigDecimal precioSesion
) {}
