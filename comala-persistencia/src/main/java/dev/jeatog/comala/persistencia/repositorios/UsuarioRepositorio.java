package dev.jeatog.comala.persistencia.repositorios;

import dev.jeatog.comala.persistencia.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepositorio extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
