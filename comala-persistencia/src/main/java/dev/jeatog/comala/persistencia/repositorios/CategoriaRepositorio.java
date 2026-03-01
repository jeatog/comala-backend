package dev.jeatog.comala.persistencia.repositorios;

import dev.jeatog.comala.persistencia.entidades.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoriaRepositorio extends JpaRepository<Categoria, UUID> {

    List<Categoria> findByNegocio_NegocioIdOrderByOrdenAsc(UUID negocioId);

    List<Categoria> findByNegocio_NegocioIdAndActivaTrueOrderByOrdenAsc(UUID negocioId);
}
