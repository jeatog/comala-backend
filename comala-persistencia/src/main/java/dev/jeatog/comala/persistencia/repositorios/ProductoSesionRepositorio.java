package dev.jeatog.comala.persistencia.repositorios;

import dev.jeatog.comala.persistencia.entidades.ProductoSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductoSesionRepositorio extends JpaRepository<ProductoSesion, UUID> {

    List<ProductoSesion> findBySesion_SesionId(UUID sesionId);

    @Query("SELECT ps FROM ProductoSesion ps JOIN FETCH ps.variante v JOIN FETCH v.producto WHERE ps.sesion.sesionId = :sesionId AND ps.disponible = true")
    List<ProductoSesion> findDisponiblesPorSesion(@Param("sesionId") UUID sesionId);
}
