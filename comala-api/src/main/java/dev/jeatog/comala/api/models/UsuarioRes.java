package dev.jeatog.comala.api.models;

import dev.jeatog.comala.negocio.dto.usuario.UsuarioDto;
import dev.jeatog.comala.persistencia.enums.RolUsuario;

import java.util.UUID;

public record UsuarioRes(
        UUID usuarioId,
        String nombre,
        String email,
        String fotoUrl,
        RolUsuario rol
) {
    public static UsuarioRes de(UsuarioDto dto) {
        return new UsuarioRes(dto.usuarioId(), dto.nombre(), dto.email(), dto.fotoUrl(), dto.rol());
    }
}
