package dev.jeatog.comala.negocio.servicios.impl;

import dev.jeatog.comala.negocio.constantes.Constantes;
import dev.jeatog.comala.negocio.dto.catalogo.ProductoDto;
import dev.jeatog.comala.negocio.dto.catalogo.VarianteDto;
import dev.jeatog.comala.negocio.excepcion.ComalaExcepcion;
import dev.jeatog.comala.negocio.servicios.ProductoServicio;
import dev.jeatog.comala.persistencia.entidades.Categoria;
import dev.jeatog.comala.persistencia.entidades.Negocio;
import dev.jeatog.comala.persistencia.entidades.Producto;
import dev.jeatog.comala.persistencia.entidades.VarianteProducto;
import dev.jeatog.comala.persistencia.repositorios.CategoriaRepositorio;
import dev.jeatog.comala.persistencia.repositorios.NegocioRepositorio;
import dev.jeatog.comala.persistencia.repositorios.ProductoRepositorio;
import dev.jeatog.comala.persistencia.repositorios.VarianteProductoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductoServicioImpl implements ProductoServicio {

    private final ProductoRepositorio productoRepositorio;
    private final CategoriaRepositorio categoriaRepositorio;
    private final NegocioRepositorio negocioRepositorio;
    private final VarianteProductoRepositorio varianteRepositorio;

    /**
     * Lista productos del negocio. Si se pasa categoriaId, filtra por esa categoría.
     *
     * @param negocioId   identificador del negocio
     * @param categoriaId filtro opcional por categoría (null = todos)
     * @return lista de productos con sus variantes
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> listar(UUID negocioId, UUID categoriaId) {
        List<Producto> productos;
        if (categoriaId != null) {
            productos = productoRepositorio.findByCategoria_CategoriaIdAndActivoTrue(categoriaId);
        } else {
            productos = productoRepositorio.findByNegocio_NegocioId(negocioId);
        }
        return productos.stream().map(this::toDto).toList();
    }

    /**
     * Obtiene un producto por ID con sus variantes.
     *
     * @param productoId identificador del producto
     * @param negocioId  identificador del negocio (validación)
     * @return producto con variantes
     * @throws ComalaExcepcion 404 si no existe, 403 si no pertenece al negocio
     */
    @Override
    @Transactional(readOnly = true)
    public ProductoDto obtenerPorId(UUID productoId, UUID negocioId) {
        return toDto(buscarYValidar(productoId, negocioId));
    }

    /**
     * Crea un producto nuevo en la categoría indicada.
     *
     * @param negocioId   identificador del negocio
     * @param categoriaId identificador de la categoría
     * @param nombre      nombre del producto
     * @return producto creado
     * @throws ComalaExcepcion 404 si el negocio o categoría no existe, 403 si la categoría no pertenece al negocio
     */
    @Override
    @Transactional
    public ProductoDto crear(UUID negocioId, UUID categoriaId, String nombre) {
        Negocio negocio = negocioRepositorio.findById(negocioId)
                .orElseThrow(() -> new ComalaExcepcion(
                        Constantes.ERR_NEGOCIO_NO_ENCONTRADO,
                        "Negocio no encontrado.",
                        Constantes.HTTP_404_NOT_FOUND
                ));

        Categoria categoria = buscarCategoriaDelNegocio(categoriaId, negocioId);

        Producto producto = new Producto();
        producto.setNegocio(negocio);
        producto.setCategoria(categoria);
        producto.setNombre(nombre);

        return toDto(productoRepositorio.save(producto));
    }

    /**
     * Edita nombre y/o categoría de un producto.
     *
     * @param productoId  identificador del producto
     * @param negocioId   identificador del negocio (validación)
     * @param nombre      nuevo nombre
     * @param categoriaId nueva categoría
     * @return producto actualizado
     * @throws ComalaExcepcion 404 si no existe, 403 si no pertenece al negocio
     */
    @Override
    @Transactional
    public ProductoDto editar(UUID productoId, UUID negocioId, String nombre, UUID categoriaId) {
        Producto producto = buscarYValidar(productoId, negocioId);
        Categoria categoria = buscarCategoriaDelNegocio(categoriaId, negocioId);

        producto.setNombre(nombre);
        producto.setCategoria(categoria);

        return toDto(productoRepositorio.save(producto));
    }

    /**
     * Actualiza la URL de la foto del producto.
     *
     * @param productoId identificador del producto
     * @param negocioId  identificador del negocio (validación)
     * @param fotoUrl    nueva URL de la foto
     * @return producto actualizado
     * @throws ComalaExcepcion 404 si no existe, 403 si no pertenece al negocio
     */
    @Override
    @Transactional
    public ProductoDto actualizarFoto(UUID productoId, UUID negocioId, String fotoUrl) {
        Producto producto = buscarYValidar(productoId, negocioId);
        producto.setImagenUrl(fotoUrl);
        return toDto(productoRepositorio.save(producto));
    }

    /**
     * Cambia el estado activo/inactivo del producto.
     *
     * @param productoId identificador del producto
     * @param negocioId  identificador del negocio (validación)
     * @param activo     nuevo estado
     * @return producto actualizado
     * @throws ComalaExcepcion 404 si no existe, 403 si no pertenece al negocio
     */
    @Override
    @Transactional
    public ProductoDto cambiarActivo(UUID productoId, UUID negocioId, boolean activo) {
        Producto producto = buscarYValidar(productoId, negocioId);
        producto.setActivo(activo);
        return toDto(productoRepositorio.save(producto));
    }

    /**
     * Soft delete del producto.
     *
     * @param productoId identificador del producto
     * @param negocioId  identificador del negocio (validación)
     * @throws ComalaExcepcion 404 si no existe, 403 si no pertenece al negocio
     */
    @Override
    @Transactional
    public void eliminar(UUID productoId, UUID negocioId) {
        Producto producto = buscarYValidar(productoId, negocioId);
        producto.setActivo(false);
        productoRepositorio.save(producto);
    }

    // --- Variantes ---

    /**
     * Lista todas las variantes de un producto.
     *
     * @param productoId identificador del producto
     * @param negocioId  identificador del negocio (validación)
     * @return lista de variantes
     * @throws ComalaExcepcion 404 si el producto no existe, 403 si no pertenece al negocio
     */
    @Override
    @Transactional(readOnly = true)
    public List<VarianteDto> listarVariantes(UUID productoId, UUID negocioId) {
        buscarYValidar(productoId, negocioId);
        return varianteRepositorio.findByProducto_ProductoId(productoId).stream()
                .map(this::toVarianteDto)
                .toList();
    }

    /**
     * Crea una variante para el producto indicado.
     *
     * @param productoId     identificador del producto
     * @param negocioId      identificador del negocio (validación)
     * @param nombreVariante nombre de la variante
     * @param precioBase     precio base de la variante
     * @return variante creada
     * @throws ComalaExcepcion 404 si el producto no existe, 403 si no pertenece al negocio
     */
    @Override
    @Transactional
    public VarianteDto crearVariante(UUID productoId, UUID negocioId, String nombreVariante, BigDecimal precioBase) {
        Producto producto = buscarYValidar(productoId, negocioId);

        VarianteProducto variante = new VarianteProducto();
        variante.setProducto(producto);
        variante.setNombreVariante(nombreVariante);
        variante.setPrecioBase(precioBase);

        return toVarianteDto(varianteRepositorio.save(variante));
    }

    /**
     * Edita nombre y precio de una variante.
     *
     * @param varianteId     identificador de la variante
     * @param negocioId      identificador del negocio (validación)
     * @param nombreVariante nuevo nombre
     * @param precioBase     nuevo precio base
     * @return variante actualizada
     * @throws ComalaExcepcion 404 si la variante no existe, 403 si no pertenece al negocio
     */
    @Override
    @Transactional
    public VarianteDto editarVariante(UUID varianteId, UUID negocioId, String nombreVariante, BigDecimal precioBase) {
        VarianteProducto variante = buscarVarianteYValidar(varianteId, negocioId);
        variante.setNombreVariante(nombreVariante);
        variante.setPrecioBase(precioBase);
        return toVarianteDto(varianteRepositorio.save(variante));
    }

    /**
     * Soft delete de una variante.
     *
     * @param varianteId identificador de la variante
     * @param negocioId  identificador del negocio (validación)
     * @throws ComalaExcepcion 404 si la variante no existe, 403 si no pertenece al negocio
     */
    @Override
    @Transactional
    public void eliminarVariante(UUID varianteId, UUID negocioId) {
        VarianteProducto variante = buscarVarianteYValidar(varianteId, negocioId);
        variante.setActivo(false);
        varianteRepositorio.save(variante);
    }

    // Métodos auxiliares
    private Producto buscarYValidar(UUID productoId, UUID negocioId) {
        Producto producto = productoRepositorio.findById(productoId)
                .orElseThrow(() -> new ComalaExcepcion(
                        Constantes.ERR_PRODUCTO_NO_ENCONTRADO,
                        "Producto no encontrado.",
                        Constantes.HTTP_404_NOT_FOUND
                ));

        if (!producto.getNegocio().getNegocioId().equals(negocioId)) {
            throw new ComalaExcepcion(
                    Constantes.ERR_ACCESO_DENEGADO,
                    "No tienes acceso a este producto.",
                    Constantes.HTTP_403_FORBIDDEN
            );
        }

        return producto;
    }

    private VarianteProducto buscarVarianteYValidar(UUID varianteId, UUID negocioId) {
        VarianteProducto variante = varianteRepositorio.findById(varianteId)
                .orElseThrow(() -> new ComalaExcepcion(
                        Constantes.ERR_VARIANTE_NO_ENCONTRADA,
                        "Variante no encontrada.",
                        Constantes.HTTP_404_NOT_FOUND
                ));

        if (!variante.getProducto().getNegocio().getNegocioId().equals(negocioId)) {
            throw new ComalaExcepcion(
                    Constantes.ERR_ACCESO_DENEGADO,
                    "No tienes acceso a esta variante.",
                    Constantes.HTTP_403_FORBIDDEN
            );
        }

        return variante;
    }

    private Categoria buscarCategoriaDelNegocio(UUID categoriaId, UUID negocioId) {
        Categoria categoria = categoriaRepositorio.findById(categoriaId)
                .orElseThrow(() -> new ComalaExcepcion(
                        Constantes.ERR_CATEGORIA_NO_ENCONTRADA,
                        "Categoria no encontrada.",
                        Constantes.HTTP_404_NOT_FOUND
                ));

        if (!categoria.getNegocio().getNegocioId().equals(negocioId)) {
            throw new ComalaExcepcion(
                    Constantes.ERR_ACCESO_DENEGADO,
                    "La categoria no pertenece a tu negocio.",
                    Constantes.HTTP_403_FORBIDDEN
            );
        }

        return categoria;
    }

    private ProductoDto toDto(Producto p) {
        List<VarianteDto> variantes = p.getVariantes() != null
                ? p.getVariantes().stream().map(this::toVarianteDto).toList()
                : List.of();

        return new ProductoDto(
                p.getProductoId(),
                p.getNombre(),
                p.getImagenUrl(),
                p.isActivo(),
                p.getCategoria().getCategoriaId(),
                p.getCategoria().getNombre(),
                variantes
        );
    }

    private VarianteDto toVarianteDto(VarianteProducto v) {
        return new VarianteDto(
                v.getVarianteId(),
                v.getNombreVariante(),
                v.getPrecioBase(),
                v.isActivo()
        );
    }
}
