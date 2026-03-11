package dev.jeatog.comala.negocio.dto.sesion;

import dev.jeatog.comala.persistencia.enums.EstatusSesion;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SesionDto(
        UUID sesionId,
        String nombre,
        LocalDate fecha,
        EstatusSesion estatus,
        BigDecimal totalCalculado,
        Instant createdAt
) {}
