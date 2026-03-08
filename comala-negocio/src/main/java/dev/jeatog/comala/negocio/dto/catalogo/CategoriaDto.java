package dev.jeatog.comala.negocio.dto.catalogo;

import java.util.UUID;

public record CategoriaDto(
        UUID categoriaId,
        String nombre,
        boolean activa,
        int orden
) {}
