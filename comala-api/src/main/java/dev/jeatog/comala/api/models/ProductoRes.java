package dev.jeatog.comala.api.models;

import dev.jeatog.comala.negocio.dto.catalogo.ProductoDto;

import java.util.List;
import java.util.UUID;

public record ProductoRes(
        UUID productoId,
        String nombre,
        String imagenUrl,
        boolean activo,
        UUID categoriaId,
        String categoriaNombre,
        List<VarianteRes> variantes
) {
    public static ProductoRes de(ProductoDto dto) {
        List<VarianteRes> vars = dto.variantes() != null
                ? dto.variantes().stream().map(VarianteRes::de).toList()
                : List.of();
        return new ProductoRes(
                dto.productoId(), dto.nombre(), dto.imagenUrl(),
                dto.activo(), dto.categoriaId(), dto.categoriaNombre(), vars
        );
    }
}
