package dev.jeatog.comala.api.models;

import dev.jeatog.comala.negocio.dto.sesion.ProductoSesionDto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductoSesionRes(
        UUID productoSesionId,
        UUID varianteId,
        String nombreVariante,
        String nombreProducto,
        BigDecimal precioSesion,
        boolean disponible
) {
    public static ProductoSesionRes de(ProductoSesionDto dto) {
        return new ProductoSesionRes(
                dto.productoSesionId(), dto.varianteId(), dto.nombreVariante(),
                dto.nombreProducto(), dto.precioSesion(), dto.disponible()
        );
    }
}
