package dev.jeatog.comala.api.servicios;

import dev.jeatog.comala.negocio.dto.usuario.RegistrarUsuarioDto;
import dev.jeatog.comala.negocio.dto.usuario.UsuarioDto;
import dev.jeatog.comala.negocio.excepcion.ComalaExcepcion;
import dev.jeatog.comala.negocio.servicios.UsuarioServicio;
import dev.jeatog.comala.persistencia.entidades.Negocio;
import dev.jeatog.comala.persistencia.enumeraciones.RolUsuario;
import dev.jeatog.comala.persistencia.repositorios.NegocioRepositorio;
import dev.jeatog.comala.persistencia.repositorios.UsuarioRepositorio;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioServicioTest {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServicioTest.class);

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private NegocioRepositorio negocioRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Test
    @Commit
    void crearUsuarioTest() {
        Negocio negocio = new Negocio();
        negocio.setNombre("Negocio de Prueba");
        negocio.setActivo(true);
        negocio = negocioRepositorio.save(negocio);
        log.info("[TEST] Negocio de prueba creado: id={}", negocio.getNegocioId());

        var dto = new RegistrarUsuarioDto(
                negocio.getNegocioId(),
                "Admin Test",
                "admin@prueba.com",
                "password123",
                RolUsuario.ADMIN
        );

        try {
            UsuarioDto resultado = usuarioServicio.registrar(dto);
            log.info("[TEST] Usuario ADMIN creado: id={}, nombre={}, email={}, rol={}",
                    resultado.usuarioId(), resultado.nombre(), resultado.email(), resultado.rol());

            assertNotNull(resultado.usuarioId());
            assertEquals("Admin Test", resultado.nombre());
            assertEquals("admin@prueba.com", resultado.email());
            assertEquals(RolUsuario.ADMIN, resultado.rol());
            assertTrue(usuarioRepositorio.existsByEmail("admin@prueba.com"));

            log.info("[TEST] Éxito.");
        } catch (Exception e) {
            log.error("[TEST] Error al crear usuario: {}", e.getMessage(), e);
            throw e;
        }
    }

}
