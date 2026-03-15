package dev.jeatog.comala.api.servicios;

import dev.jeatog.comala.negocio.dto.cobro.CobrarDto;
import dev.jeatog.comala.negocio.dto.cobro.CobroDto;
import dev.jeatog.comala.negocio.dto.pedido.CrearPedidoDto;
import dev.jeatog.comala.negocio.dto.pedido.PedidoDto;
import dev.jeatog.comala.negocio.dto.pedido.PedidoLineaEntradaDto;
import dev.jeatog.comala.negocio.dto.sesion.CrearSesionDto;
import dev.jeatog.comala.negocio.dto.sesion.ProductoSesionEntradaDto;
import dev.jeatog.comala.negocio.excepcion.ComalaExcepcion;
import dev.jeatog.comala.negocio.servicios.CobroServicio;
import dev.jeatog.comala.negocio.servicios.PedidoServicio;
import dev.jeatog.comala.negocio.servicios.SesionServicio;
import dev.jeatog.comala.persistencia.entidades.Categoria;
import dev.jeatog.comala.persistencia.entidades.Negocio;
import dev.jeatog.comala.persistencia.entidades.Producto;
import dev.jeatog.comala.persistencia.entidades.Usuario;
import dev.jeatog.comala.persistencia.entidades.VarianteProducto;
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
class CobroServicioTest {

    @Autowired private CobroServicio cobroServicio;
    @Autowired private PedidoServicio pedidoServicio;
    @Autowired private SesionServicio sesionServicio;
    @Autowired private NegocioRepositorio negocioRepositorio;
    @Autowired private UsuarioRepositorio usuarioRepositorio;
    @Autowired private CategoriaRepositorio categoriaRepositorio;
    @Autowired private ProductoRepositorio productoRepositorio;
    @Autowired private VarianteProductoRepositorio varianteRepositorio;

    private Negocio negocio;
    private UUID sesionId;
    private UUID varianteId;

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
        usuario = usuarioRepositorio.save(usuario);

        Categoria cat = new Categoria();
        cat.setNegocio(negocio);
        cat.setNombre("Tamales");
        cat.setActiva(true);
        cat.setOrden(0);
        cat = categoriaRepositorio.save(cat);

        Producto prod = new Producto();
        prod.setNegocio(negocio);
        prod.setCategoria(cat);
        prod.setNombre("Tamal Rajas");
        prod.setActivo(true);
        prod = productoRepositorio.save(prod);

        VarianteProducto variante = new VarianteProducto();
        variante.setProducto(prod);
        variante.setNombreVariante("Grande");
        variante.setPrecioBase(new BigDecimal("25.00"));
        variante.setActiva(true);
        variante = varianteRepositorio.save(variante);
        varianteId = variante.getVarianteId();

        // Crear sesion
        var sesionDto = sesionServicio.crear(new CrearSesionDto(
                negocio.getNegocioId(), usuario.getUsuarioId(), "Sesion Test",
                List.of(new ProductoSesionEntradaDto(varianteId, new BigDecimal("30.00")))
        ));
        sesionId = sesionDto.sesionId();
    }

    private PedidoDto crearPedidoFiado(LocalDate fechaCompromiso) {
        return pedidoServicio.crear(new CrearPedidoDto(
                sesionId, "Cliente Fiado", "Dir 123",
                MetodoPago.FIADO, MetodoSolicitud.WHATSAPP, TipoEnvio.NORMAL,
                fechaCompromiso,
                List.of(new PedidoLineaEntradaDto(varianteId, 2, null))
        ), negocio.getNegocioId());
    }

    private PedidoDto crearPedidoEfectivo() {
        return pedidoServicio.crear(new CrearPedidoDto(
                sesionId, "Cliente Cash", null,
                MetodoPago.EFECTIVO, MetodoSolicitud.EN_PERSONA, TipoEnvio.PASA_CLIENTE,
                null,
                List.of(new PedidoLineaEntradaDto(varianteId, 1, null))
        ), negocio.getNegocioId());
    }

    @Test
    void listarFiados_sinFiltro_devuelvePendientes() {
        crearPedidoFiado(LocalDate.now().plusDays(5));
        crearPedidoFiado(LocalDate.now().minusDays(2));
        crearPedidoEfectivo(); // este no debe aparecer

        List<CobroDto> resultado = cobroServicio.listarFiados(negocio.getNegocioId(), null);

        assertEquals(2, resultado.size());
    }

    @Test
    void listarFiados_filtroVencido() {
        crearPedidoFiado(LocalDate.now().minusDays(3));
        crearPedidoFiado(LocalDate.now().plusDays(5));

        List<CobroDto> vencidos = cobroServicio.listarFiados(negocio.getNegocioId(), "VENCIDO");

        assertEquals(1, vencidos.size());
        assertTrue(vencidos.getFirst().fechaCompromiso().isBefore(LocalDate.now()));
    }

    @Test
    void listarFiados_filtroProximo() {
        crearPedidoFiado(LocalDate.now().minusDays(3));
        crearPedidoFiado(LocalDate.now().plusDays(5));

        List<CobroDto> proximos = cobroServicio.listarFiados(negocio.getNegocioId(), "PROXIMO");

        assertEquals(1, proximos.size());
        assertFalse(proximos.getFirst().fechaCompromiso().isBefore(LocalDate.now()));
    }

    @Test
    void cobrar_exitoso() {
        PedidoDto pedido = crearPedidoFiado(LocalDate.now().plusDays(3));

        CobroDto resultado = cobroServicio.cobrar(new CobrarDto(
                pedido.pedidoId(), negocio.getNegocioId(), MetodoPago.EFECTIVO
        ));

        assertTrue(resultado.pagado());
        assertEquals(MetodoPago.EFECTIVO, resultado.metodoPago());
        assertNotNull(resultado.fechaPagoReal());
    }

    @Test
    void cobrar_filtroCobrado_aparece() {
        PedidoDto pedido = crearPedidoFiado(LocalDate.now().plusDays(3));
        cobroServicio.cobrar(new CobrarDto(
                pedido.pedidoId(), negocio.getNegocioId(), MetodoPago.TRANSFERENCIA
        ));

        List<CobroDto> cobrados = cobroServicio.listarFiados(negocio.getNegocioId(), "COBRADO");

        assertEquals(1, cobrados.size());
        assertEquals(MetodoPago.TRANSFERENCIA, cobrados.getFirst().metodoPago());
    }

    @Test
    void cobrar_yaPagado_lanzaExcepcion() {
        PedidoDto pedido = crearPedidoFiado(LocalDate.now().plusDays(3));
        cobroServicio.cobrar(new CobrarDto(
                pedido.pedidoId(), negocio.getNegocioId(), MetodoPago.EFECTIVO
        ));

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class, () ->
                cobroServicio.cobrar(new CobrarDto(
                        pedido.pedidoId(), negocio.getNegocioId(), MetodoPago.EFECTIVO
                ))
        );
        assertEquals(409, ex.getHttpStatus());
    }

    @Test
    void cobrar_noEsFiado_lanzaExcepcion() {
        PedidoDto pedido = crearPedidoEfectivo();

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class, () ->
                cobroServicio.cobrar(new CobrarDto(
                        pedido.pedidoId(), negocio.getNegocioId(), MetodoPago.EFECTIVO
                ))
        );
        assertEquals(400, ex.getHttpStatus());
    }

    @Test
    void cobrar_metodoFiado_lanzaExcepcion() {
        PedidoDto pedido = crearPedidoFiado(LocalDate.now().plusDays(3));

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class, () ->
                cobroServicio.cobrar(new CobrarDto(
                        pedido.pedidoId(), negocio.getNegocioId(), MetodoPago.FIADO
                ))
        );
        assertEquals(400, ex.getHttpStatus());
    }

    @Test
    void cobrar_otroNegocio_lanzaExcepcion() {
        PedidoDto pedido = crearPedidoFiado(LocalDate.now().plusDays(3));

        ComalaExcepcion ex = assertThrows(ComalaExcepcion.class, () ->
                cobroServicio.cobrar(new CobrarDto(
                        pedido.pedidoId(), UUID.randomUUID(), MetodoPago.EFECTIVO
                ))
        );
        assertEquals(403, ex.getHttpStatus());
    }
}
