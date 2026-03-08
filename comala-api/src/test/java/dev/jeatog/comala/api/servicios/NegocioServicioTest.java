package dev.jeatog.comala.api.servicios;

import dev.jeatog.comala.negocio.dto.negocio.NegocioDto;
import dev.jeatog.comala.negocio.excepcion.ComalaExcepcion;
import dev.jeatog.comala.negocio.servicios.NegocioServicio;
import dev.jeatog.comala.persistencia.entidades.Negocio;
import dev.jeatog.comala.persistencia.repositorios.NegocioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NegocioServicioTest {

    @Autowired
    private NegocioServicio negocioServicio;

    @Autowired
    private NegocioRepositorio negocioRepositorio;

    private Negocio negocio;

    @BeforeEach
    void setUp() {
        negocio = new Negocio();
        negocio.setNombre("Comala Original");
        negocio.setActivo(true);
        negocio = negocioRepositorio.save(negocio);
    }

    @Test
    void obtener_exitoso() {
        NegocioDto resultado = negocioServicio.obtener(negocio.getNegocioId());

        assertEquals(negocio.getNegocioId(), resultado.negocioId());
        assertEquals("Comala Original", resultado.nombre());
    }

    @Test
    void obtener_inexistente_lanza404() {
        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> negocioServicio.obtener(UUID.randomUUID()));
        assertEquals(404, ex.getHttpStatus());
    }

    @Test
    void editarNombre_exitoso() {
        NegocioDto resultado = negocioServicio.editarNombre(negocio.getNegocioId(), "Comala Nuevo");

        assertEquals("Comala Nuevo", resultado.nombre());
        assertEquals(negocio.getNegocioId(), resultado.negocioId());
    }

    @Test
    void editarNombre_inexistente_lanza404() {
        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> negocioServicio.editarNombre(UUID.randomUUID(), "Nombre"));
        assertEquals(404, ex.getHttpStatus());
    }
}
