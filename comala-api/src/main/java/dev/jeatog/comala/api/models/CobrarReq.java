package dev.jeatog.comala.api.models;

import dev.jeatog.comala.persistencia.enums.MetodoPago;
import jakarta.validation.constraints.NotNull;

public record CobrarReq(
        @NotNull MetodoPago metodoPagoReal
) {}
