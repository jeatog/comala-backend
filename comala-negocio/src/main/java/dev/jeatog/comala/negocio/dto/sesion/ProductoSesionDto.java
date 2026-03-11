package dev.jeatog.comala.negocio.dto.sesion;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductoSesionDto(
        UUID productoSesionId,
        UUID varianteId,
        String nombreVariante,
        String nombreProducto,
        BigDecimal precioSesion,
        boolean disponible
) {}
