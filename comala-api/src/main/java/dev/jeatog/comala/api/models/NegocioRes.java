package dev.jeatog.comala.api.models;

import dev.jeatog.comala.negocio.dto.negocio.NegocioDto;

import java.util.UUID;

public record NegocioRes(
        UUID negocioId,
        String nombre
) {
    public static NegocioRes de(NegocioDto dto) {
        return new NegocioRes(dto.negocioId(), dto.nombre());
    }
}
