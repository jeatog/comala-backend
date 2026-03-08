package dev.jeatog.comala.negocio.dto.auth;

import dev.jeatog.comala.persistencia.enums.RolUsuario;

import java.util.UUID;

public record NegocioResumenDto(
        UUID negocioId,
        String nombre,
        RolUsuario rol
) {}
