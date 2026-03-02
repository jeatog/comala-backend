package dev.jeatog.comala.negocio.dto.usuario;

import dev.jeatog.comala.persistencia.enumeraciones.RolUsuario;

import java.util.UUID;

public record UsuarioDto(
        UUID usuarioId,
        String nombre,
        String email,
        String fotoUrl,
        RolUsuario rol
) {}
