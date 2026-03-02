package dev.jeatog.comala.negocio.servicios;

import dev.jeatog.comala.negocio.dto.usuario.RegistrarUsuarioDto;
import dev.jeatog.comala.negocio.dto.usuario.UsuarioDto;

import java.util.UUID;

public interface UsuarioServicio {

    UsuarioDto registrar(RegistrarUsuarioDto dto);
    UsuarioDto actualizarFoto(UUID usuarioId, UUID negocioId, String fotoUrl);
    
}
