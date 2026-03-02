package dev.jeatog.comala.negocio.dto.auth;

import dev.jeatog.comala.persistencia.enumeraciones.RolUsuario;

import java.util.UUID;

public record NegocioResumenDto(
        UUID negocioId,
        String nombre,
        RolUsuario rol
) {}
