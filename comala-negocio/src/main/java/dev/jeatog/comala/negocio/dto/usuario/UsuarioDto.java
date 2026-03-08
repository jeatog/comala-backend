package dev.jeatog.comala.negocio.dto.usuario;

import dev.jeatog.comala.persistencia.enums.RolUsuario;

import java.util.UUID;

public record UsuarioDto(
        UUID usuarioId,
        String nombre,
        String email,
        String fotoUrl,
        RolUsuario rol
) {}
