package dev.jeatog.comala.api.models;

import dev.jeatog.comala.negocio.dto.sesion.SesionDto;
import dev.jeatog.comala.persistencia.enums.EstatusSesion;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SesionRes(
        UUID sesionId,
        String nombre,
        LocalDate fecha,
        EstatusSesion estatus,
        BigDecimal totalCalculado,
        Instant createdAt
) {
    public static SesionRes de(SesionDto dto) {
        return new SesionRes(
                dto.sesionId(), dto.nombre(), dto.fecha(),
                dto.estatus(), dto.totalCalculado(), dto.createdAt()
        );
    }
}
