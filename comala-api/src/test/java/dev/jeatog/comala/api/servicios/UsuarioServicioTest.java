package dev.jeatog.comala.api.servicios;

import dev.jeatog.comala.negocio.dto.usuario.RegistrarUsuarioDto;
import dev.jeatog.comala.negocio.dto.usuario.UsuarioDto;
import dev.jeatog.comala.negocio.excepcion.ComalaExcepcion;
import dev.jeatog.comala.negocio.servicios.UsuarioServicio;
import dev.jeatog.comala.persistencia.entidades.Negocio;
import dev.jeatog.comala.persistencia.entidades.NegocioUsuario;
import dev.jeatog.comala.persistencia.entidades.Usuario;
import dev.jeatog.comala.persistencia.enums.RolUsuario;
import dev.jeatog.comala.persistencia.repositorios.NegocioRepositorio;
import dev.jeatog.comala.persistencia.repositorios.NegocioUsuarioRepositorio;
import dev.jeatog.comala.persistencia.repositorios.UsuarioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioServicioTest {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private NegocioRepositorio negocioRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private NegocioUsuarioRepositorio negocioUsuarioRepositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Negocio negocio;

    @BeforeEach
    void setUp() {
        negocio = new Negocio();
        negocio.setNombre("Negocio Test");
        negocio.setActivo(true);
        negocio = negocioRepositorio.save(negocio);
    }

    @Test
    void registrar_exitoso() {
        var dto = new RegistrarUsuarioDto(
                negocio.getNegocioId(), "Admin Test", "admin@test.com", "password123", RolUsuario.ADMIN
        );

        UsuarioDto resultado = usuarioServicio.registrar(dto);

        assertNotNull(resultado.usuarioId());
        assertEquals("Admin Test", resultado.nombre());
        assertEquals("admin@test.com", resultado.email());
        assertEquals(RolUsuario.ADMIN, resultado.rol());
        assertTrue(usuarioRepositorio.existsByEmail("admin@test.com"));
    }

    @Test
    void registrar_emailDuplicado_lanza409() {
        var dto = new RegistrarUsuarioDto(
                negocio.getNegocioId(), "Primero", "duplicado@test.com", "password123", RolUsuario.ADMIN
        );
        usuarioServicio.registrar(dto);

        var dtoDuplicado = new RegistrarUsuarioDto(
                negocio.getNegocioId(), "Segundo", "duplicado@test.com", "password123", RolUsuario.OPERADOR
        );

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class, () -> usuarioServicio.registrar(dtoDuplicado));
        assertEquals(409, ex.getHttpStatus());
    }

    @Test
    void registrar_negocioInexistente_lanza404() {
        var dto = new RegistrarUsuarioDto(
                UUID.randomUUID(), "Test", "test@test.com", "password123", RolUsuario.ADMIN
        );

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class, () -> usuarioServicio.registrar(dto));
        assertEquals(404, ex.getHttpStatus());
    }

    @Test
    void actualizarFoto_exitoso() {
        var dto = new RegistrarUsuarioDto(
                negocio.getNegocioId(), "Foto User", "foto@test.com", "password123", RolUsuario.OPERADOR
        );
        UsuarioDto creado = usuarioServicio.registrar(dto);

        UsuarioDto actualizado = usuarioServicio.actualizarFoto(
                creado.usuarioId(), negocio.getNegocioId(), "http://ejemplo.com/foto.jpg"
        );

        assertEquals("http://ejemplo.com/foto.jpg", actualizado.fotoUrl());
    }

    @Test
    void actualizarFoto_usuarioDeOtroNegocio_lanza403() {
        var dto = new RegistrarUsuarioDto(
                negocio.getNegocioId(), "User", "user@test.com", "password123", RolUsuario.OPERADOR
        );
        UsuarioDto creado = usuarioServicio.registrar(dto);

        Negocio otroNegocio = new Negocio();
        otroNegocio.setNombre("Otro Negocio");
        otroNegocio.setActivo(true);
        otroNegocio = negocioRepositorio.save(otroNegocio);

        UUID otroNegocioId = otroNegocio.getNegocioId();
        UUID usuarioId = creado.usuarioId();

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> usuarioServicio.actualizarFoto(usuarioId, otroNegocioId, "http://foto.jpg"));
        assertEquals(403, ex.getHttpStatus());
    }

    @Test
    void cambiarNombre_exitoso() {
        var dto = new RegistrarUsuarioDto(
                negocio.getNegocioId(), "Nombre Original", "nombre@test.com", "password123", RolUsuario.ADMIN
        );
        usuarioServicio.registrar(dto);

        UsuarioDto resultado = usuarioServicio.cambiarNombre(
                "nombre@test.com", negocio.getNegocioId(), "Nombre Nuevo"
        );

        assertEquals("Nombre Nuevo", resultado.nombre());
    }

    @Test
    void cambiarPassword_exitoso() {
        var dto = new RegistrarUsuarioDto(
                negocio.getNegocioId(), "Pass User", "pass@test.com", "password123", RolUsuario.ADMIN
        );
        usuarioServicio.registrar(dto);

        assertDoesNotThrow(() ->
                usuarioServicio.cambiarPassword("pass@test.com", "password123", "nuevaPassword456")
        );

        Usuario usuario = usuarioRepositorio.findByEmail("pass@test.com").orElseThrow();
        assertTrue(passwordEncoder.matches("nuevaPassword456", usuario.getPasswordHash()));
    }

    @Test
    void cambiarPassword_passwordActualIncorrecta_lanza401() {
        var dto = new RegistrarUsuarioDto(
                negocio.getNegocioId(), "Pass User", "pass2@test.com", "password123", RolUsuario.ADMIN
        );
        usuarioServicio.registrar(dto);

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> usuarioServicio.cambiarPassword("pass2@test.com", "incorrecta", "nueva123456")
        );
        assertEquals(401, ex.getHttpStatus());
    }

    @Test
    void cambiarFotoPropia_exitoso() {
        var dto = new RegistrarUsuarioDto(
                negocio.getNegocioId(), "Foto Propia", "fotopropia@test.com", "password123", RolUsuario.OPERADOR
        );
        usuarioServicio.registrar(dto);

        UsuarioDto resultado = usuarioServicio.cambiarFotoPropia(
                "fotopropia@test.com", negocio.getNegocioId(), "http://ejemplo.com/mi-foto.jpg"
        );

        assertEquals("http://ejemplo.com/mi-foto.jpg", resultado.fotoUrl());
    }
}
