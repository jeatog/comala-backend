package dev.jeatog.comala.api.models;

import dev.jeatog.comala.negocio.dto.auth.NegocioResumenDto;

import java.util.List;

public record LoginRes(
        /**
         * Token de acceso completo si requiereSeleccion = false.
         * Token de selección (corto plazo) si requiereSeleccion = true.
         */
        String token,
        boolean requiereSeleccion,
        List<NegocioResumenDto> negocios
) {}
