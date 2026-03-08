package dev.jeatog.comala.negocio.servicios;

import dev.jeatog.comala.negocio.dto.catalogo.ProductoDto;
import dev.jeatog.comala.negocio.dto.catalogo.VarianteDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductoServicio {

    List<ProductoDto> listar(UUID negocioId, UUID categoriaId);
    ProductoDto obtenerPorId(UUID productoId, UUID negocioId);
    ProductoDto crear(UUID negocioId, UUID categoriaId, String nombre);
    ProductoDto editar(UUID productoId, UUID negocioId, String nombre, UUID categoriaId);
    ProductoDto actualizarFoto(UUID productoId, UUID negocioId, String fotoUrl);
    ProductoDto cambiarActivo(UUID productoId, UUID negocioId, boolean activo);
    void eliminar(UUID productoId, UUID negocioId);
    List<VarianteDto> listarVariantes(UUID productoId, UUID negocioId);
    VarianteDto crearVariante(UUID productoId, UUID negocioId, String nombreVariante, BigDecimal precioBase);
    VarianteDto editarVariante(UUID varianteId, UUID negocioId, String nombreVariante, BigDecimal precioBase);
    void eliminarVariante(UUID varianteId, UUID negocioId);
    
}
