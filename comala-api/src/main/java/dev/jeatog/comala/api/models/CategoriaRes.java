package dev.jeatog.comala.api.models;

import dev.jeatog.comala.negocio.dto.catalogo.CategoriaDto;

import java.util.UUID;

public record CategoriaRes(
        UUID categoriaId,
        String nombre,
        boolean activa,
        int orden
) {
    public static CategoriaRes de(CategoriaDto dto) {
        return new CategoriaRes(dto.categoriaId(), dto.nombre(), dto.activa(), dto.orden());
    }
}
