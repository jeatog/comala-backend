package dev.jeatog.comala.negocio.dto.sesion;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductoSesionEntradaDto(
        UUID varianteId,
        BigDecimal precioSesion
) {}
