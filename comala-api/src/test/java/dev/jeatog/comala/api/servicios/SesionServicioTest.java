package dev.jeatog.comala.api.servicios;

import dev.jeatog.comala.negocio.dto.sesion.CrearSesionDto;
import dev.jeatog.comala.negocio.dto.sesion.ProductoSesionDto;
import dev.jeatog.comala.negocio.dto.sesion.ProductoSesionEntradaDto;
import dev.jeatog.comala.negocio.dto.sesion.SesionDto;
import dev.jeatog.comala.negocio.excepcion.ComalaExcepcion;
import dev.jeatog.comala.negocio.servicios.SesionServicio;
import dev.jeatog.comala.persistencia.entidades.Categoria;
import dev.jeatog.comala.persistencia.entidades.Negocio;
import dev.jeatog.comala.persistencia.entidades.Producto;
import dev.jeatog.comala.persistencia.entidades.Usuario;
import dev.jeatog.comala.persistencia.entidades.VarianteProducto;
import dev.jeatog.comala.persistencia.enums.EstatusSesion;
import dev.jeatog.comala.persistencia.repositorios.CategoriaRepositorio;
import dev.jeatog.comala.persistencia.repositorios.NegocioRepositorio;
import dev.jeatog.comala.persistencia.repositorios.ProductoRepositorio;
import dev.jeatog.comala.persistencia.repositorios.UsuarioRepositorio;
import dev.jeatog.comala.persistencia.repositorios.VarianteProductoRepositorio;
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
class SesionServicioTest {

    @Autowired
    private SesionServicio sesionServicio;

    @Autowired
    private NegocioRepositorio negocioRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private CategoriaRepositorio categoriaRepositorio;

    @Autowired
    private ProductoRepositorio productoRepositorio;

    @Autowired
    private VarianteProductoRepositorio varianteRepositorio;

    private Negocio negocio;
    private Usuario usuario;
    private VarianteProducto variante1;
    private VarianteProducto variante2;

    @BeforeEach
    void setUp() {
        negocio = new Negocio();
        negocio.setNombre("Negocio Test");
        negocio.setActivo(true);
        negocio = negocioRepositorio.save(negocio);

        usuario = new Usuario();
        usuario.setNombre("Admin Test");
        usuario.setEmail("admin@test.com");
        usuario.setPasswordHash("hash");
        usuario.setActivo(true);
        usuario = usuarioRepositorio.save(usuario);

        Categoria categoria = new Categoria();
        categoria.setNegocio(negocio);
        categoria.setNombre("Tamales");
        categoria.setOrden(0);
        categoria = categoriaRepositorio.save(categoria);

        Producto producto = new Producto();
        producto.setNegocio(negocio);
        producto.setCategoria(categoria);
        producto.setNombre("Tamal de Rajas");
        producto = productoRepositorio.save(producto);

        variante1 = new VarianteProducto();
        variante1.setProducto(producto);
        variante1.setNombreVariante("Chico");
        variante1.setPrecioBase(new BigDecimal("15.00"));
        variante1 = varianteRepositorio.save(variante1);

        variante2 = new VarianteProducto();
        variante2.setProducto(producto);
        variante2.setNombreVariante("Grande");
        variante2.setPrecioBase(new BigDecimal("25.00"));
        variante2 = varianteRepositorio.save(variante2);
    }

    private CrearSesionDto crearDtoSesion() {
        return new CrearSesionDto(
                negocio.getNegocioId(),
                usuario.getUsuarioId(),
                "Sesion del dia",
                List.of(
                        new ProductoSesionEntradaDto(variante1.getVarianteId(), new BigDecimal("18.00")),
                        new ProductoSesionEntradaDto(variante2.getVarianteId(), new BigDecimal("28.00"))
                )
        );
    }

    @Test
    void crear_exitoso() {
        SesionDto resultado = sesionServicio.crear(crearDtoSesion());

        assertNotNull(resultado.sesionId());
        assertEquals("Sesion del dia", resultado.nombre());
        assertEquals(EstatusSesion.EN_CURSO, resultado.estatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(resultado.totalCalculado()));
    }

    @Test
    void crear_conSesionActivaExistente_lanza409() {
        sesionServicio.crear(crearDtoSesion());

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> sesionServicio.crear(crearDtoSesion()));
        assertEquals(409, ex.getHttpStatus());
    }

    @Test
    void crear_negocioInexistente_lanza404() {
        var dto = new CrearSesionDto(UUID.randomUUID(), usuario.getUsuarioId(), "Test", List.of());

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class, () -> sesionServicio.crear(dto));
        assertEquals(404, ex.getHttpStatus());
    }

    @Test
    void obtenerActiva_conSesion() {
        sesionServicio.crear(crearDtoSesion());

        SesionDto activa = sesionServicio.obtenerActiva(negocio.getNegocioId());
        assertNotNull(activa);
        assertEquals(EstatusSesion.EN_CURSO, activa.estatus());
    }

    @Test
    void obtenerActiva_sinSesion_devuelveNull() {
        SesionDto activa = sesionServicio.obtenerActiva(negocio.getNegocioId());
        assertNull(activa);
    }

    @Test
    void finalizar_exitoso() {
        SesionDto creada = sesionServicio.crear(crearDtoSesion());

        SesionDto finalizada = sesionServicio.finalizar(creada.sesionId(), negocio.getNegocioId());

        assertEquals(EstatusSesion.FINALIZADA, finalizada.estatus());
    }

    @Test
    void finalizar_sesionYaFinalizada_lanza409() {
        SesionDto creada = sesionServicio.crear(crearDtoSesion());
        sesionServicio.finalizar(creada.sesionId(), negocio.getNegocioId());

        UUID sesionId = creada.sesionId();
        UUID negocioId = negocio.getNegocioId();

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> sesionServicio.finalizar(sesionId, negocioId));
        assertEquals(409, ex.getHttpStatus());
    }

    @Test
    void finalizar_deOtroNegocio_lanza403() {
        SesionDto creada = sesionServicio.crear(crearDtoSesion());

        Negocio otro = new Negocio();
        otro.setNombre("Otro");
        otro.setActivo(true);
        otro = negocioRepositorio.save(otro);

        UUID sesionId = creada.sesionId();
        UUID otroId = otro.getNegocioId();

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> sesionServicio.finalizar(sesionId, otroId));
        assertEquals(403, ex.getHttpStatus());
    }

    @Test
    void obtenerPorId_exitoso() {
        SesionDto creada = sesionServicio.crear(crearDtoSesion());

        SesionDto resultado = sesionServicio.obtenerPorId(creada.sesionId(), negocio.getNegocioId());
        assertEquals(creada.sesionId(), resultado.sesionId());
    }

    @Test
    void listarFinalizadas_devuelveSoloFinalizadas() {
        SesionDto creada = sesionServicio.crear(crearDtoSesion());
        sesionServicio.finalizar(creada.sesionId(), negocio.getNegocioId());

        List<SesionDto> finalizadas = sesionServicio.listarFinalizadas(negocio.getNegocioId());
        assertEquals(1, finalizadas.size());
        assertEquals(EstatusSesion.FINALIZADA, finalizadas.get(0).estatus());
    }

    @Test
    void listarProductosSesion_exitoso() {
        SesionDto creada = sesionServicio.crear(crearDtoSesion());

        List<ProductoSesionDto> productos = sesionServicio.listarProductosSesion(
                creada.sesionId(), negocio.getNegocioId()
        );

        assertEquals(2, productos.size());
    }
}
