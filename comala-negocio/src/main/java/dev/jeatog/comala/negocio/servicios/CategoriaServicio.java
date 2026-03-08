package dev.jeatog.comala.negocio.servicios;

import dev.jeatog.comala.negocio.dto.catalogo.CategoriaDto;

import java.util.List;
import java.util.UUID;

public interface CategoriaServicio {

    List<CategoriaDto> listar(UUID negocioId);
    CategoriaDto crear(UUID negocioId, String nombre);
    CategoriaDto editar(UUID categoriaId, UUID negocioId, String nombre);
    void eliminar(UUID categoriaId, UUID negocioId);
    
}
