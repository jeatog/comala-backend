package dev.jeatog.comala.api.servicios;

import dev.jeatog.comala.negocio.dto.pedido.CrearPedidoDto;
import dev.jeatog.comala.negocio.dto.pedido.EditarPedidoDto;
import dev.jeatog.comala.negocio.dto.pedido.PedidoDto;
import dev.jeatog.comala.negocio.dto.pedido.PedidoLineaEntradaDto;
import dev.jeatog.comala.negocio.dto.sesion.CrearSesionDto;
import dev.jeatog.comala.negocio.dto.sesion.ProductoSesionEntradaDto;
import dev.jeatog.comala.negocio.dto.sesion.SesionDto;
import dev.jeatog.comala.negocio.excepcion.ComalaExcepcion;
import dev.jeatog.comala.negocio.servicios.PedidoServicio;
import dev.jeatog.comala.negocio.servicios.SesionServicio;
import dev.jeatog.comala.persistencia.entidades.Categoria;
import dev.jeatog.comala.persistencia.entidades.Negocio;
import dev.jeatog.comala.persistencia.entidades.Producto;
import dev.jeatog.comala.persistencia.entidades.Usuario;
import dev.jeatog.comala.persistencia.entidades.VarianteProducto;
import dev.jeatog.comala.persistencia.enums.EstatusPedido;
import dev.jeatog.comala.persistencia.enums.MetodoPago;
import dev.jeatog.comala.persistencia.enums.MetodoSolicitud;
import dev.jeatog.comala.persistencia.enums.TipoEnvio;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PedidoServicioTest {

    @Autowired
    private PedidoServicio pedidoServicio;

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
    private SesionDto sesion;
    private VarianteProducto variante;

    @BeforeEach
    void setUp() {
        negocio = new Negocio();
        negocio.setNombre("Negocio Test");
        negocio.setActivo(true);
        negocio = negocioRepositorio.save(negocio);

        Usuario usuario = new Usuario();
        usuario.setNombre("Admin");
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

        variante = new VarianteProducto();
        variante.setProducto(producto);
        variante.setNombreVariante("Grande");
        variante.setPrecioBase(new BigDecimal("25.00"));
        variante = varianteRepositorio.save(variante);

        sesion = sesionServicio.crear(new CrearSesionDto(
                negocio.getNegocioId(),
                usuario.getUsuarioId(),
                "Sesion Test",
                List.of(new ProductoSesionEntradaDto(variante.getVarianteId(), new BigDecimal("30.00")))
        ));
    }

    private CrearPedidoDto crearDtoPedido() {
        return new CrearPedidoDto(
                sesion.sesionId(),
                "Juan Perez",
                "Calle 123",
                MetodoPago.EFECTIVO,
                MetodoSolicitud.WHATSAPP,
                TipoEnvio.NORMAL,
                null,
                List.of(new PedidoLineaEntradaDto(variante.getVarianteId(), 3, null))
        );
    }

    @Test
    void crear_exitoso() {
        PedidoDto resultado = pedidoServicio.crear(crearDtoPedido(), negocio.getNegocioId());

        assertNotNull(resultado.pedidoId());
        assertEquals("Juan Perez", resultado.nombreCliente());
        assertEquals(EstatusPedido.RECIEN_PEDIDO, resultado.estatus());
        assertEquals(0, new BigDecimal("90.00").compareTo(resultado.total()));
        assertEquals(1, resultado.lineas().size());
        assertEquals(3, resultado.lineas().get(0).cantidad());
        assertEquals(0, new BigDecimal("30.00").compareTo(resultado.lineas().get(0).precioUnitario()));
    }

    @Test
    void crear_sinLineas_lanza400() {
        var dto = new CrearPedidoDto(
                sesion.sesionId(), "Cliente", null,
                MetodoPago.EFECTIVO, MetodoSolicitud.EN_PERSONA, TipoEnvio.PASA_CLIENTE,
                null, List.of()
        );

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> pedidoServicio.crear(dto, negocio.getNegocioId()));
        assertEquals(400, ex.getHttpStatus());
    }

    @Test
    void crear_fiadoSinFechaCompromiso_lanza400() {
        var dto = new CrearPedidoDto(
                sesion.sesionId(), "Cliente", null,
                MetodoPago.FIADO, MetodoSolicitud.LLAMADA, TipoEnvio.PROPIO,
                null,
                List.of(new PedidoLineaEntradaDto(variante.getVarianteId(), 1, null))
        );

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> pedidoServicio.crear(dto, negocio.getNegocioId()));
        assertEquals(400, ex.getHttpStatus());
    }

    @Test
    void crear_fiadoConFechaCompromiso_exitoso() {
        var dto = new CrearPedidoDto(
                sesion.sesionId(), "Fiado Cliente", null,
                MetodoPago.FIADO, MetodoSolicitud.LLAMADA, TipoEnvio.PROPIO,
                LocalDate.now().plusDays(7),
                List.of(new PedidoLineaEntradaDto(variante.getVarianteId(), 2, null))
        );

        PedidoDto resultado = pedidoServicio.crear(dto, negocio.getNegocioId());
        assertEquals(MetodoPago.FIADO, resultado.metodoPago());
        assertFalse(resultado.pagado());
        assertNotNull(resultado.fechaCompromiso());
    }

    @Test
    void crear_varianteNoDisponible_lanza404() {
        var dto = new CrearPedidoDto(
                sesion.sesionId(), "Cliente", null,
                MetodoPago.EFECTIVO, MetodoSolicitud.EN_PERSONA, TipoEnvio.PASA_CLIENTE,
                null,
                List.of(new PedidoLineaEntradaDto(UUID.randomUUID(), 1, null))
        );

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> pedidoServicio.crear(dto, negocio.getNegocioId()));
        assertEquals(404, ex.getHttpStatus());
    }

    @Test
    void editar_exitoso() {
        PedidoDto creado = pedidoServicio.crear(crearDtoPedido(), negocio.getNegocioId());

        var editarDto = new EditarPedidoDto(
                creado.pedidoId(),
                negocio.getNegocioId(),
                "Maria Lopez",
                "Calle 456",
                MetodoPago.TRANSFERENCIA,
                MetodoSolicitud.LLAMADA,
                TipoEnvio.PROPIO,
                null,
                List.of(new PedidoLineaEntradaDto(variante.getVarianteId(), 5, "Extra salsa"))
        );

        PedidoDto editado = pedidoServicio.editar(editarDto);

        assertEquals("Maria Lopez", editado.nombreCliente());
        assertEquals(MetodoPago.TRANSFERENCIA, editado.metodoPago());
        assertEquals(0, new BigDecimal("150.00").compareTo(editado.total()));
        assertEquals("Extra salsa", editado.lineas().get(0).notas());
    }

    @Test
    void cambiarEstatus_exitoso() {
        PedidoDto creado = pedidoServicio.crear(crearDtoPedido(), negocio.getNegocioId());

        PedidoDto preparando = pedidoServicio.cambiarEstatus(
                creado.pedidoId(), negocio.getNegocioId(), EstatusPedido.PREPARANDO
        );
        assertEquals(EstatusPedido.PREPARANDO, preparando.estatus());

        PedidoDto entregado = pedidoServicio.cambiarEstatus(
                creado.pedidoId(), negocio.getNegocioId(), EstatusPedido.ENTREGADO
        );
        assertEquals(EstatusPedido.ENTREGADO, entregado.estatus());
        assertTrue(entregado.pagado());
    }

    @Test
    void cambiarEstatus_fiadoNoSeMarcaPagado() {
        var dto = new CrearPedidoDto(
                sesion.sesionId(), "Fiado", null,
                MetodoPago.FIADO, MetodoSolicitud.EN_PERSONA, TipoEnvio.PASA_CLIENTE,
                LocalDate.now().plusDays(7),
                List.of(new PedidoLineaEntradaDto(variante.getVarianteId(), 1, null))
        );
        PedidoDto creado = pedidoServicio.crear(dto, negocio.getNegocioId());

        PedidoDto entregado = pedidoServicio.cambiarEstatus(
                creado.pedidoId(), negocio.getNegocioId(), EstatusPedido.ENTREGADO
        );
        assertFalse(entregado.pagado());
    }

    @Test
    void eliminar_exitoso() {
        PedidoDto creado = pedidoServicio.crear(crearDtoPedido(), negocio.getNegocioId());

        pedidoServicio.eliminar(creado.pedidoId(), negocio.getNegocioId());

        UUID pedidoId = creado.pedidoId();
        UUID negocioId = negocio.getNegocioId();
        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> pedidoServicio.obtenerPorId(pedidoId, negocioId));
        assertEquals(404, ex.getHttpStatus());
    }

    @Test
    void eliminar_deOtroNegocio_lanza403() {
        PedidoDto creado = pedidoServicio.crear(crearDtoPedido(), negocio.getNegocioId());

        Negocio otro = new Negocio();
        otro.setNombre("Otro");
        otro.setActivo(true);
        otro = negocioRepositorio.save(otro);

        UUID pedidoId = creado.pedidoId();
        UUID otroId = otro.getNegocioId();

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class,
                () -> pedidoServicio.eliminar(pedidoId, otroId));
        assertEquals(403, ex.getHttpStatus());
    }

    @Test
    void listarPorSesion_exitoso() {
        pedidoServicio.crear(crearDtoPedido(), negocio.getNegocioId());
        pedidoServicio.crear(new CrearPedidoDto(
                sesion.sesionId(), "Otro Cliente", null,
                MetodoPago.TRANSFERENCIA, MetodoSolicitud.LLAMADA, TipoEnvio.PROPIO,
                null,
                List.of(new PedidoLineaEntradaDto(variante.getVarianteId(), 1, null))
        ), negocio.getNegocioId());

        List<PedidoDto> pedidos = pedidoServicio.listarPorSesion(
                sesion.sesionId(), negocio.getNegocioId()
        );
        assertEquals(2, pedidos.size());
    }

    @Test
    void totalSesion_seActualizaAlCrearPedido() {
        pedidoServicio.crear(crearDtoPedido(), negocio.getNegocioId());

        SesionDto sesionActualizada = sesionServicio.obtenerPorId(
                sesion.sesionId(), negocio.getNegocioId()
        );
        assertEquals(0, new BigDecimal("90.00").compareTo(sesionActualizada.totalCalculado()));
    }

    @Test
    void totalSesion_seActualizaAlEliminarPedido() {
        PedidoDto creado = pedidoServicio.crear(crearDtoPedido(), negocio.getNegocioId());
        pedidoServicio.eliminar(creado.pedidoId(), negocio.getNegocioId());

        SesionDto sesionActualizada = sesionServicio.obtenerPorId(
                sesion.sesionId(), negocio.getNegocioId()
        );
        assertEquals(0, BigDecimal.ZERO.compareTo(sesionActualizada.totalCalculado()));
    }
}
