package dev.jeatog.comala.negocio.dto.pedido;

import dev.jeatog.comala.persistencia.enums.MetodoPago;
import dev.jeatog.comala.persistencia.enums.MetodoSolicitud;
import dev.jeatog.comala.persistencia.enums.TipoEnvio;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CrearPedidoDto(
        UUID sesionId,
        String nombreCliente,
        String direccion,
        MetodoPago metodoPago,
        MetodoSolicitud metodoSolicitud,
        TipoEnvio tipoEnvio,
        LocalDate fechaCompromiso,
        List<PedidoLineaEntradaDto> lineas
) {}
