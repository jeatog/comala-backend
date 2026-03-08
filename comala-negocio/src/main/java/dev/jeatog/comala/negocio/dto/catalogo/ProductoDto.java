package dev.jeatog.comala.negocio.dto.catalogo;

import java.util.List;
import java.util.UUID;

public record ProductoDto(
        UUID productoId,
        String nombre,
        String imagenUrl,
        boolean activo,
        UUID categoriaId,
        String categoriaNombre,
        List<VarianteDto> variantes
) {}
