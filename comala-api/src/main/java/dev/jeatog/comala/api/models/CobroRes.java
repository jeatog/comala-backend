package dev.jeatog.comala.api.models;

import dev.jeatog.comala.negocio.dto.cobro.CobroDto;
import dev.jeatog.comala.persistencia.enums.MetodoPago;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CobroRes(
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
) {
    public static CobroRes de(CobroDto dto) {
        return new CobroRes(
                dto.pedidoId(), dto.sesionId(), dto.nombreSesion(),
                dto.nombreCliente(), dto.direccion(), dto.total(),
                dto.fechaCompromiso(), dto.metodoPago(), dto.pagado(),
                dto.fechaPagoReal(), dto.createdAt()
        );
    }
}
