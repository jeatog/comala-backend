package dev.jeatog.comala.negocio.dto.usuario;

import dev.jeatog.comala.persistencia.enumeraciones.RolUsuario;

import java.util.UUID;

public record RegistrarUsuarioDto(
        UUID negocioId,
        String nombre,
        String email,
        String password,
        RolUsuario rol
) {}
