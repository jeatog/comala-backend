package dev.jeatog.comala.persistencia.repositorios;

import dev.jeatog.comala.persistencia.entidades.Sesion;
import dev.jeatog.comala.persistencia.enumeraciones.EstatusSesion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SesionRepositorio extends JpaRepository<Sesion, UUID> {

    Optional<Sesion> findByNegocio_NegocioIdAndEstatus(UUID negocioId, EstatusSesion estatus);

    boolean existsByNegocio_NegocioIdAndEstatus(UUID negocioId, EstatusSesion estatus);

    List<Sesion> findByNegocio_NegocioIdOrderByCreatedAtDesc(UUID negocioId);
}
