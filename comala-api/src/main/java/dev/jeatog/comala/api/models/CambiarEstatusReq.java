package dev.jeatog.comala.api.models;

import dev.jeatog.comala.persistencia.enums.EstatusPedido;
import jakarta.validation.constraints.NotNull;

public record CambiarEstatusReq(
        @NotNull EstatusPedido estatus
) {}
