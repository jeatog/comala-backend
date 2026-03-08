package dev.jeatog.comala.api.models;

import dev.jeatog.comala.negocio.dto.catalogo.VarianteDto;

import java.math.BigDecimal;
import java.util.UUID;

public record VarianteRes(
        UUID varianteId,
        String nombreVariante,
        BigDecimal precioBase,
        boolean activo
) {
    public static VarianteRes de(VarianteDto dto) {
        return new VarianteRes(dto.varianteId(), dto.nombreVariante(), dto.precioBase(), dto.activo());
    }
}
