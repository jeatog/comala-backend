package dev.jeatog.comala.negocio.dto.cobro;

import dev.jeatog.comala.persistencia.enums.MetodoPago;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CobroDto(
        UUID pedidoId,
        UUID sesionId,
        String nombreSesion,
        String nombreCliente,
        String direccion,
        BigDecimal total,
        LocalDate fechaCompromiso,
        MetodoPago metodoPago,
        boolean pagado,
        Instant fechaPagoReal,
        Instant createdAt
) {}
