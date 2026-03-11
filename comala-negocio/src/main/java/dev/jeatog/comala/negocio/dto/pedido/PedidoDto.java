package dev.jeatog.comala.negocio.dto.pedido;

import dev.jeatog.comala.persistencia.enums.EstatusPedido;
import dev.jeatog.comala.persistencia.enums.MetodoPago;
import dev.jeatog.comala.persistencia.enums.MetodoSolicitud;
import dev.jeatog.comala.persistencia.enums.TipoEnvio;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PedidoDto(
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
        List<PedidoLineaDto> lineas
) {}
