package dev.jeatog.comala.api.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PedidoLineaReq(
        @NotNull UUID varianteId,
        @Min(1) int cantidad,
        String notas
) {}
