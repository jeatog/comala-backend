package dev.jeatog.comala.api.models;

import dev.jeatog.comala.persistencia.enums.MetodoPago;
import dev.jeatog.comala.persistencia.enums.MetodoSolicitud;
import dev.jeatog.comala.persistencia.enums.TipoEnvio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record EditarPedidoReq(
        String nombreCliente,
        String direccion,
        @NotNull MetodoPago metodoPago,
        @NotNull MetodoSolicitud metodoSolicitud,
        @NotNull TipoEnvio tipoEnvio,
        LocalDate fechaCompromiso,
        @NotEmpty @Valid List<PedidoLineaReq> lineas
) {}
