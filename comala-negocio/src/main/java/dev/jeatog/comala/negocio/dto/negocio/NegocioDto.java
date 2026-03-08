package dev.jeatog.comala.negocio.dto.negocio;

import java.util.UUID;

public record NegocioDto(
        UUID negocioId,
        String nombre
) {}
