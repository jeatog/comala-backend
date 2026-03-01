package dev.jeatog.comala.persistencia.repositorios;

import dev.jeatog.comala.persistencia.entidades.VarianteProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VarianteProductoRepositorio extends JpaRepository<VarianteProducto, UUID> {

    List<VarianteProducto> findByProducto_ProductoIdAndActivoTrue(UUID productoId);

    List<VarianteProducto> findByProducto_ProductoId(UUID productoId);
}
