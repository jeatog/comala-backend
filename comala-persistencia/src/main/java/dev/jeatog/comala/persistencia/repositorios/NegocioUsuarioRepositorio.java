package dev.jeatog.comala.persistencia.repositorios;

import dev.jeatog.comala.persistencia.entidades.NegocioUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NegocioUsuarioRepositorio extends JpaRepository<NegocioUsuario, UUID> {

    List<NegocioUsuario> findByUsuario_UsuarioIdAndActivoTrue(UUID usuarioId);

    Optional<NegocioUsuario> findByNegocio_NegocioIdAndUsuario_UsuarioIdAndActivoTrue(
            UUID negocioId, UUID usuarioId
    );

    @Query("SELECT nu FROM NegocioUsuario nu JOIN FETCH nu.negocio WHERE nu.usuario.usuarioId = :usuarioId AND nu.activo = true")
    List<NegocioUsuario> findNegociosActivosPorUsuario(@Param("usuarioId") UUID usuarioId);
}
