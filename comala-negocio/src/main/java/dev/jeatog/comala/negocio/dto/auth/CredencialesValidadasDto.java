package dev.jeatog.comala.negocio.dto.auth;

import java.util.List;
import java.util.UUID;

public record CredencialesValidadasDto(
        UUID usuarioId,
        String nombre,
        String email,
        List<NegocioResumenDto> negocios
) {}
