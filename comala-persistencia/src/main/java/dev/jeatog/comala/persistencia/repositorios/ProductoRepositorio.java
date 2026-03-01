package dev.jeatog.comala.persistencia.repositorios;

import dev.jeatog.comala.persistencia.entidades.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductoRepositorio extends JpaRepository<Producto, UUID> {

    List<Producto> findByNegocio_NegocioId(UUID negocioId);

    List<Producto> findByCategoria_CategoriaIdAndActivoTrue(UUID categoriaId);

    List<Producto> findByNegocio_NegocioIdAndActivoTrue(UUID negocioId);
}
