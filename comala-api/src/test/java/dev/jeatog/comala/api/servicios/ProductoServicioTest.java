package dev.jeatog.comala.api.servicios;

import dev.jeatog.comala.negocio.dto.catalogo.CategoriaDto;
import dev.jeatog.comala.negocio.dto.catalogo.ProductoDto;
import dev.jeatog.comala.negocio.dto.catalogo.VarianteDto;
import dev.jeatog.comala.negocio.excepcion.ComalaExcepcion;
import dev.jeatog.comala.negocio.servicios.CategoriaServicio;
import dev.jeatog.comala.negocio.servicios.ProductoServicio;
import dev.jeatog.comala.persistencia.entidades.Negocio;
import dev.jeatog.comala.persistencia.repositorios.NegocioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductoServicioTest {

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private CategoriaServicio categoriaServicio;

    @Autowired
    private NegocioRepositorio negocioRepositorio;

    private Negocio negocio;
    private CategoriaDto categoria;

    @BeforeEach
    void setUp() {
        negocio = new Negocio();
        negocio.setNombre("Negocio Test");
        negocio.setActivo(true);
        negocio = negocioRepositorio.save(negocio);

        categoria = categoriaServicio.crear(negocio.getNegocioId(), "Tamales");
    }

    // --- Producto ---

    @Test
    void crear_exitoso() {
        ProductoDto resultado = productoServicio.crear(
                negocio.getNegocioId(), categoria.categoriaId(), "Tamal de Rajas"
        );

        assertNotNull(resultado.productoId());
        assertEquals("Tamal de Rajas", resultado.nombre());
        assertTrue(resultado.activo());
        assertEquals(categoria.categoriaId(), resultado.categoriaId());
        assertEquals("Tamales", resultado.categoriaNombre());
    }

    @Test
    void crear_negocioInexistente_lanza404() {
        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> productoServicio.crear(UUID.randomUUID(), categoria.categoriaId(), "Producto"));
        assertEquals(404, ex.getHttpStatus());
    }

    @Test
    void crear_categoriaInexistente_lanza404() {
        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> productoServicio.crear(negocio.getNegocioId(), UUID.randomUUID(), "Producto"));
        assertEquals(404, ex.getHttpStatus());
    }

    @Test
    void crear_categoriaDeOtroNegocio_lanza403() {
        Negocio otroNegocio = new Negocio();
        otroNegocio.setNombre("Otro");
        otroNegocio.setActivo(true);
        otroNegocio = negocioRepositorio.save(otroNegocio);

        UUID otroId = otroNegocio.getNegocioId();

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> productoServicio.crear(otroId, categoria.categoriaId(), "Producto"));
        assertEquals(403, ex.getHttpStatus());
    }

    @Test
    void listar_todosDelNegocio() {
        productoServicio.crear(negocio.getNegocioId(), categoria.categoriaId(), "Producto 1");
        productoServicio.crear(negocio.getNegocioId(), categoria.categoriaId(), "Producto 2");

        List<ProductoDto> resultado = productoServicio.listar(negocio.getNegocioId(), null);
        assertEquals(2, resultado.size());
    }

    @Test
    void listar_filtradoPorCategoria() {
        CategoriaDto otraCategoria = categoriaServicio.crear(negocio.getNegocioId(), "Bebidas");

        productoServicio.crear(negocio.getNegocioId(), categoria.categoriaId(), "Tamal");
        productoServicio.crear(negocio.getNegocioId(), otraCategoria.categoriaId(), "Agua");

        List<ProductoDto> tamales = productoServicio.listar(negocio.getNegocioId(), categoria.categoriaId());
        assertEquals(1, tamales.size());
        assertEquals("Tamal", tamales.get(0).nombre());
    }

    @Test
    void obtenerPorId_exitoso() {
        ProductoDto creado = productoServicio.crear(
                negocio.getNegocioId(), categoria.categoriaId(), "Tamal Verde"
        );

        ProductoDto resultado = productoServicio.obtenerPorId(creado.productoId(), negocio.getNegocioId());

        assertEquals(creado.productoId(), resultado.productoId());
        assertEquals("Tamal Verde", resultado.nombre());
    }

    @Test
    void obtenerPorId_productoDeOtroNegocio_lanza403() {
        ProductoDto creado = productoServicio.crear(
                negocio.getNegocioId(), categoria.categoriaId(), "Protegido"
        );

        Negocio otroNegocio = new Negocio();
        otroNegocio.setNombre("Otro");
        otroNegocio.setActivo(true);
        otroNegocio = negocioRepositorio.save(otroNegocio);

        UUID otroId = otroNegocio.getNegocioId();
        UUID prodId = creado.productoId();

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> productoServicio.obtenerPorId(prodId, otroId));
        assertEquals(403, ex.getHttpStatus());
    }

    @Test
    void editar_exitoso() {
        ProductoDto creado = productoServicio.crear(
                negocio.getNegocioId(), categoria.categoriaId(), "Original"
        );
        CategoriaDto nuevaCategoria = categoriaServicio.crear(negocio.getNegocioId(), "Bebidas");

        ProductoDto editado = productoServicio.editar(
                creado.productoId(), negocio.getNegocioId(), "Editado", nuevaCategoria.categoriaId()
        );

        assertEquals("Editado", editado.nombre());
        assertEquals(nuevaCategoria.categoriaId(), editado.categoriaId());
    }

    @Test
    void actualizarFoto_exitoso() {
        ProductoDto creado = productoServicio.crear(
                negocio.getNegocioId(), categoria.categoriaId(), "Con Foto"
        );

        ProductoDto resultado = productoServicio.actualizarFoto(
                creado.productoId(), negocio.getNegocioId(), "http://ejemplo.com/foto.jpg"
        );

        assertEquals("http://ejemplo.com/foto.jpg", resultado.imagenUrl());
    }

    @Test
    void cambiarActivo_exitoso() {
        ProductoDto creado = productoServicio.crear(
                negocio.getNegocioId(), categoria.categoriaId(), "Activable"
        );
        assertTrue(creado.activo());

        ProductoDto desactivado = productoServicio.cambiarActivo(
                creado.productoId(), negocio.getNegocioId(), false
        );
        assertFalse(desactivado.activo());

        ProductoDto reactivado = productoServicio.cambiarActivo(
                creado.productoId(), negocio.getNegocioId(), true
        );
        assertTrue(reactivado.activo());
    }

    @Test
    void eliminar_softDelete() {
        ProductoDto creado = productoServicio.crear(
                negocio.getNegocioId(), categoria.categoriaId(), "A Eliminar"
        );

        productoServicio.eliminar(creado.productoId(), negocio.getNegocioId());

        ProductoDto eliminado = productoServicio.obtenerPorId(creado.productoId(), negocio.getNegocioId());
        assertFalse(eliminado.activo());
    }

    // --- Variantes ---

    @Test
    void crearVariante_exitoso() {
        ProductoDto producto = productoServicio.crear(
                negocio.getNegocioId(), categoria.categoriaId(), "Tamal"
        );

        VarianteDto variante = productoServicio.crearVariante(
                producto.productoId(), negocio.getNegocioId(), "Grande", new BigDecimal("25.00")
        );

        assertNotNull(variante.varianteId());
        assertEquals("Grande", variante.nombreVariante());
        assertEquals(0, new BigDecimal("25.00").compareTo(variante.precioBase()));
        assertTrue(variante.activo());
    }

    @Test
    void crearVariante_productoInexistente_lanza404() {
        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> productoServicio.crearVariante(
                        UUID.randomUUID(), negocio.getNegocioId(), "Grande", new BigDecimal("25.00")
                ));
        assertEquals(404, ex.getHttpStatus());
    }

    @Test
    void listarVariantes_exitoso() {
        ProductoDto producto = productoServicio.crear(
                negocio.getNegocioId(), categoria.categoriaId(), "Tamal"
        );
        productoServicio.crearVariante(producto.productoId(), negocio.getNegocioId(), "Chico", new BigDecimal("15.00"));
        productoServicio.crearVariante(producto.productoId(), negocio.getNegocioId(), "Grande", new BigDecimal("25.00"));

        List<VarianteDto> variantes = productoServicio.listarVariantes(producto.productoId(), negocio.getNegocioId());

        assertEquals(2, variantes.size());
    }

    @Test
    void editarVariante_exitoso() {
        ProductoDto producto = productoServicio.crear(
                negocio.getNegocioId(), categoria.categoriaId(), "Tamal"
        );
        VarianteDto creada = productoServicio.crearVariante(
                producto.productoId(), negocio.getNegocioId(), "Original", new BigDecimal("20.00")
        );

        VarianteDto editada = productoServicio.editarVariante(
                creada.varianteId(), negocio.getNegocioId(), "Editada", new BigDecimal("30.00")
        );

        assertEquals("Editada", editada.nombreVariante());
        assertEquals(0, new BigDecimal("30.00").compareTo(editada.precioBase()));
    }

    @Test
    void editarVariante_deOtroNegocio_lanza403() {
        ProductoDto producto = productoServicio.crear(
                negocio.getNegocioId(), categoria.categoriaId(), "Tamal"
        );
        VarianteDto creada = productoServicio.crearVariante(
                producto.productoId(), negocio.getNegocioId(), "Variante", new BigDecimal("20.00")
        );

        Negocio otroNegocio = new Negocio();
        otroNegocio.setNombre("Otro");
        otroNegocio.setActivo(true);
        otroNegocio = negocioRepositorio.save(otroNegocio);

        UUID otroId = otroNegocio.getNegocioId();
        UUID varianteId = creada.varianteId();

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> productoServicio.editarVariante(varianteId, otroId, "Hack", new BigDecimal("1.00")));
        assertEquals(403, ex.getHttpStatus());
    }

    @Test
    void eliminarVariante_softDelete() {
        ProductoDto producto = productoServicio.crear(
                negocio.getNegocioId(), categoria.categoriaId(), "Tamal"
        );
        VarianteDto creada = productoServicio.crearVariante(
                producto.productoId(), negocio.getNegocioId(), "A Eliminar", new BigDecimal("20.00")
        );

        productoServicio.eliminarVariante(creada.varianteId(), negocio.getNegocioId());

        List<VarianteDto> variantes = productoServicio.listarVariantes(producto.productoId(), negocio.getNegocioId());
        VarianteDto eliminada = variantes.stream()
                .filter(v -> v.varianteId().equals(creada.varianteId()))
                .findFirst()
                .orElseThrow();
        assertFalse(eliminada.activo());
    }
}
