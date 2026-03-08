package dev.jeatog.comala.api.servicios;

import dev.jeatog.comala.negocio.dto.catalogo.CategoriaDto;
import dev.jeatog.comala.negocio.excepcion.ComalaExcepcion;
import dev.jeatog.comala.negocio.servicios.CategoriaServicio;
import dev.jeatog.comala.persistencia.entidades.Negocio;
import dev.jeatog.comala.persistencia.repositorios.NegocioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoriaServicioTest {

    @Autowired
    private CategoriaServicio categoriaServicio;

    @Autowired
    private NegocioRepositorio negocioRepositorio;

    private Negocio negocio;

    @BeforeEach
    void setUp() {
        negocio = new Negocio();
        negocio.setNombre("Negocio Test");
        negocio.setActivo(true);
        negocio = negocioRepositorio.save(negocio);
    }

    @Test
    void crear_exitoso() {
        CategoriaDto resultado = categoriaServicio.crear(negocio.getNegocioId(), "Tamales");

        assertNotNull(resultado.categoriaId());
        assertEquals("Tamales", resultado.nombre());
        assertTrue(resultado.activa());
        assertEquals(0, resultado.orden());
    }

    @Test
    void crear_ordenAutoIncremental() {
        categoriaServicio.crear(negocio.getNegocioId(), "Primera");
        categoriaServicio.crear(negocio.getNegocioId(), "Segunda");
        CategoriaDto tercera = categoriaServicio.crear(negocio.getNegocioId(), "Tercera");

        assertEquals(2, tercera.orden());
    }

    @Test
    void crear_negocioInexistente_lanza404() {
        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> categoriaServicio.crear(UUID.randomUUID(), "Categoria"));
        assertEquals(404, ex.getHttpStatus());
    }

    @Test
    void listar_devuelveCategoriasOrdenadas() {
        categoriaServicio.crear(negocio.getNegocioId(), "Bebidas");
        categoriaServicio.crear(negocio.getNegocioId(), "Tamales");
        categoriaServicio.crear(negocio.getNegocioId(), "Postres");

        List<CategoriaDto> resultado = categoriaServicio.listar(negocio.getNegocioId());

        assertEquals(3, resultado.size());
        assertEquals("Bebidas", resultado.get(0).nombre());
        assertEquals("Tamales", resultado.get(1).nombre());
        assertEquals("Postres", resultado.get(2).nombre());
    }

    @Test
    void listar_sinCategorias_devuelveListaVacia() {
        List<CategoriaDto> resultado = categoriaServicio.listar(negocio.getNegocioId());
        assertTrue(resultado.isEmpty());
    }

    @Test
    void editar_exitoso() {
        CategoriaDto creada = categoriaServicio.crear(negocio.getNegocioId(), "Nombre Original");

        CategoriaDto editada = categoriaServicio.editar(
                creada.categoriaId(), negocio.getNegocioId(), "Nombre Editado"
        );

        assertEquals("Nombre Editado", editada.nombre());
        assertEquals(creada.categoriaId(), editada.categoriaId());
    }

    @Test
    void editar_categoriaInexistente_lanza404() {
        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> categoriaServicio.editar(UUID.randomUUID(), negocio.getNegocioId(), "Nuevo"));
        assertEquals(404, ex.getHttpStatus());
    }

    @Test
    void editar_categoriaDeOtroNegocio_lanza403() {
        CategoriaDto creada = categoriaServicio.crear(negocio.getNegocioId(), "Mi Categoria");

        Negocio otroNegocio = new Negocio();
        otroNegocio.setNombre("Otro Negocio");
        otroNegocio.setActivo(true);
        otroNegocio = negocioRepositorio.save(otroNegocio);

        UUID otroId = otroNegocio.getNegocioId();
        UUID catId = creada.categoriaId();

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> categoriaServicio.editar(catId, otroId, "Hackeado"));
        assertEquals(403, ex.getHttpStatus());
    }

    @Test
    void eliminar_softDelete() {
        CategoriaDto creada = categoriaServicio.crear(negocio.getNegocioId(), "A Eliminar");

        categoriaServicio.eliminar(creada.categoriaId(), negocio.getNegocioId());

        List<CategoriaDto> todas = categoriaServicio.listar(negocio.getNegocioId());
        CategoriaDto eliminada = todas.stream()
                .filter(c -> c.categoriaId().equals(creada.categoriaId()))
                .findFirst()
                .orElseThrow();
        assertFalse(eliminada.activa());
    }

    @Test
    void eliminar_categoriaDeOtroNegocio_lanza403() {
        CategoriaDto creada = categoriaServicio.crear(negocio.getNegocioId(), "Protegida");

        Negocio otroNegocio = new Negocio();
        otroNegocio.setNombre("Otro");
        otroNegocio.setActivo(true);
        otroNegocio = negocioRepositorio.save(otroNegocio);

        UUID otroId = otroNegocio.getNegocioId();
        UUID catId = creada.categoriaId();

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> categoriaServicio.eliminar(catId, otroId));
        assertEquals(403, ex.getHttpStatus());
    }
}
