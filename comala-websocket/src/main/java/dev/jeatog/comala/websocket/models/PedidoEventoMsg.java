package dev.jeatog.comala.websocket.models;

import java.util.UUID;

public record PedidoEventoMsg(
        TipoEventoPedido tipo,
        UUID pedidoId,
        Object payload
) {
    public enum TipoEventoPedido {
        PEDIDO_CREADO,
        PEDIDO_ACTUALIZADO,
        ESTATUS_CAMBIADO,
        PEDIDO_ELIMINADO
    }
}
