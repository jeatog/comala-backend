package dev.jeatog.comala.negocio.dto.catalogo;

import java.math.BigDecimal;
import java.util.UUID;

public record VarianteDto(
        UUID varianteId,
        String nombreVariante,
        BigDecimal precioBase,
        boolean activo
) {}
