package dev.jeatog.comala.persistencia.repositorios;

import dev.jeatog.comala.persistencia.entidades.Negocio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NegocioRepositorio extends JpaRepository<Negocio, UUID> {
}
