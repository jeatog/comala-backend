package dev.jeatog.comala.negocio.servicios.impl;

import dev.jeatog.comala.negocio.constantes.Constantes;
import dev.jeatog.comala.negocio.dto.catalogo.CategoriaDto;
import dev.jeatog.comala.negocio.excepcion.ComalaExcepcion;
import dev.jeatog.comala.negocio.servicios.CategoriaServicio;
import dev.jeatog.comala.persistencia.entidades.Categoria;
import dev.jeatog.comala.persistencia.entidades.Negocio;
import dev.jeatog.comala.persistencia.repositorios.CategoriaRepositorio;
import dev.jeatog.comala.persistencia.repositorios.NegocioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoriaServicioImpl implements CategoriaServicio {

    private final CategoriaRepositorio categoriaRepositorio;
    private final NegocioRepositorio negocioRepositorio;

    /**
     * Lista todas las categorías del negocio (activas e inactivas), ordenadas por orden.
     *
     * @param negocioId identificador del negocio
     * @return lista de categorías
     */
    @Override
    public List<CategoriaDto> listar(UUID negocioId) {
        return categoriaRepositorio.findByNegocio_NegocioIdOrderByOrdenAsc(negocioId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Crea una categoría nueva al final del orden.
     *
     * @param negocioId identificador del negocio
     * @param nombre    nombre de la categoría
     * @return categoría creada
     * @throws ComalaExcepcion 404 si el negocio no existe
     */
    @Override
    @Transactional
    public CategoriaDto crear(UUID negocioId, String nombre) {
        Negocio negocio = negocioRepositorio.findById(negocioId)
                .orElseThrow(() -> new ComalaExcepcion(
                        Constantes.ERR_NEGOCIO_NO_ENCONTRADO,
                        "Negocio no encontrado.",
                        Constantes.HTTP_404_NOT_FOUND
                ));

        int siguienteOrden = categoriaRepositorio
                .findByNegocio_NegocioIdOrderByOrdenAsc(negocioId).size();

        Categoria categoria = new Categoria();
        categoria.setNegocio(negocio);
        categoria.setNombre(nombre);
        categoria.setOrden(siguienteOrden);

        return toDto(categoriaRepositorio.save(categoria));
    }

    /**
     * Edita el nombre de una categoría existente.
     *
     * @param categoriaId identificador de la categoría
     * @param negocioId   identificador del negocio (validación de pertenencia)
     * @param nombre      nuevo nombre
     * @return categoría actualizada
     * @throws ComalaExcepcion 404 si no existe, 403 si no pertenece al negocio
     */
    @Override
    @Transactional
    public CategoriaDto editar(UUID categoriaId, UUID negocioId, String nombre) {
        Categoria categoria = buscarYValidar(categoriaId, negocioId);
        categoria.setNombre(nombre);
        return toDto(categoriaRepositorio.save(categoria));
    }

    /**
     * Soft delete de una categoría.
     *
     * @param categoriaId identificador de la categoría
     * @param negocioId   identificador del negocio (validación de pertenencia)
     * @throws ComalaExcepcion 404 si no existe, 403 si no pertenece al negocio
     */
    @Override
    @Transactional
    public void eliminar(UUID categoriaId, UUID negocioId) {
        Categoria categoria = buscarYValidar(categoriaId, negocioId);
        categoria.setActiva(false);
        categoriaRepositorio.save(categoria);
    }

    private Categoria buscarYValidar(UUID categoriaId, UUID negocioId) {
        Categoria categoria = categoriaRepositorio.findById(categoriaId)
                .orElseThrow(() -> new ComalaExcepcion(
                        Constantes.ERR_CATEGORIA_NO_ENCONTRADA,
                        "Categoria no encontrada.",
                        Constantes.HTTP_404_NOT_FOUND
                ));

        if (!categoria.getNegocio().getNegocioId().equals(negocioId)) {
            throw new ComalaExcepcion(
                    Constantes.ERR_ACCESO_DENEGADO,
                    "No tienes acceso a esta categoria.",
                    Constantes.HTTP_403_FORBIDDEN
            );
        }

        return categoria;
    }

    private CategoriaDto toDto(Categoria cat) {
        return new CategoriaDto(
                cat.getCategoriaId(),
                cat.getNombre(),
                cat.isActiva(),
                cat.getOrden()
        );
    }
}
