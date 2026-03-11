package dev.jeatog.comala.api.models;

import dev.jeatog.comala.negocio.dto.pedido.PedidoDto;
import dev.jeatog.comala.persistencia.enums.EstatusPedido;
import dev.jeatog.comala.persistencia.enums.MetodoPago;
import dev.jeatog.comala.persistencia.enums.MetodoSolicitud;
import dev.jeatog.comala.persistencia.enums.TipoEnvio;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PedidoRes(
        UUID pedidoId,
        UUID sesionId,
        String nombreCliente,
        String direccion,
        EstatusPedido estatus,
        MetodoPago metodoPago,
        MetodoSolicitud metodoSolicitud,
        TipoEnvio tipoEnvio,
        BigDecimal total,
        boolean pagado,
        LocalDate fechaCompromiso,
        Instant createdAt,
        List<PedidoLineaRes> lineas
) {
    public static PedidoRes de(PedidoDto dto) {
        List<PedidoLineaRes> lineas = dto.lineas() != null
                ? dto.lineas().stream().map(PedidoLineaRes::de).toList()
                : List.of();

        return new PedidoRes(
                dto.pedidoId(), dto.sesionId(), dto.nombreCliente(), dto.direccion(),
                dto.estatus(), dto.metodoPago(), dto.metodoSolicitud(), dto.tipoEnvio(),
                dto.total(), dto.pagado(), dto.fechaCompromiso(), dto.createdAt(), lineas
        );
    }
}
