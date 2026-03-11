package dev.jeatog.comala.negocio.dto.pedido;

import java.util.UUID;

public record PedidoLineaEntradaDto(
        UUID varianteId,
        int cantidad,
        String notas
) {}
