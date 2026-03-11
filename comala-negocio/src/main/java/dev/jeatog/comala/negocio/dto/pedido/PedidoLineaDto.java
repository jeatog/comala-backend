package dev.jeatog.comala.negocio.dto.pedido;

import java.math.BigDecimal;
import java.util.UUID;

public record PedidoLineaDto(
        UUID lineaId,
        UUID varianteId,
        String nombreVariante,
        String nombreProducto,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        String notas
) {}
