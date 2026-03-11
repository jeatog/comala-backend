package dev.jeatog.comala.api.models;

import dev.jeatog.comala.negocio.dto.pedido.PedidoLineaDto;

import java.math.BigDecimal;
import java.util.UUID;

public record PedidoLineaRes(
        UUID lineaId,
        UUID varianteId,
        String nombreVariante,
        String nombreProducto,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        String notas
) {
    public static PedidoLineaRes de(PedidoLineaDto dto) {
        return new PedidoLineaRes(
                dto.lineaId(), dto.varianteId(), dto.nombreVariante(),
                dto.nombreProducto(), dto.cantidad(), dto.precioUnitario(),
                dto.subtotal(), dto.notas()
        );
    }
}
