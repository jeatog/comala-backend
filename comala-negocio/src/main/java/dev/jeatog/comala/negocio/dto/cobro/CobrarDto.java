package dev.jeatog.comala.negocio.dto.cobro;

import dev.jeatog.comala.persistencia.enums.MetodoPago;

import java.util.UUID;

public record CobrarDto(
        UUID pedidoId,
        UUID negocioId,
        MetodoPago metodoPagoReal
) {}
