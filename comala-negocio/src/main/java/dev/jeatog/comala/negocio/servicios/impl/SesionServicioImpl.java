package dev.jeatog.comala.negocio.servicios.impl;

import dev.jeatog.comala.negocio.constantes.Constantes;
import dev.jeatog.comala.negocio.dto.sesion.CrearSesionDto;
import dev.jeatog.comala.negocio.dto.sesion.ProductoSesionDto;
import dev.jeatog.comala.negocio.dto.sesion.SesionDto;
import dev.jeatog.comala.negocio.excepcion.ComalaExcepcion;
import dev.jeatog.comala.negocio.servicios.SesionServicio;
import dev.jeatog.comala.persistencia.entidades.Negocio;
import dev.jeatog.comala.persistencia.entidades.ProductoSesion;
import dev.jeatog.comala.persistencia.entidades.Sesion;
import dev.jeatog.comala.persistencia.entidades.Usuario;
import dev.jeatog.comala.persistencia.entidades.VarianteProducto;
import dev.jeatog.comala.persistencia.enums.EstatusPedido;
import dev.jeatog.comala.persistencia.enums.EstatusSesion;
import dev.jeatog.comala.persistencia.repositorios.NegocioRepositorio;
import dev.jeatog.comala.persistencia.repositorios.PedidoRepositorio;
import dev.jeatog.comala.persistencia.repositorios.ProductoSesionRepositorio;
import dev.jeatog.comala.persistencia.repositorios.SesionRepositorio;
import dev.jeatog.comala.persistencia.repositorios.UsuarioRepositorio;
import dev.jeatog.comala.persistencia.repositorios.VarianteProductoRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SesionServicioImpl implements SesionServicio {

    private final SesionRepositorio sesionRepositorio;
    private final NegocioRepositorio negocioRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final VarianteProductoRepositorio varianteRepositorio;
    private final ProductoSesionRepositorio productoSesionRepositorio;
    private final PedidoRepositorio pedidoRepositorio;

    /**
     * Obtiene la sesion EN_CURSO del negocio, o null si no hay ninguna.
     *
     * @param negocioId identificador del negocio
     * @return sesion activa o null
     */
    @Override
    @Transactional(readOnly = true)
    public SesionDto obtenerActiva(UUID negocioId) {
        return sesionRepositorio
                .findByNegocio_NegocioIdAndEstatus(negocioId, EstatusSesion.EN_CURSO)
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * Crea una sesion nueva con productos y precios ajustados. Valida que no exista otra EN_CURSO.
     *
     * @param dto datos de la sesion a crear
     * @return sesion creada
     * @throws ComalaExcepcion 409 si ya hay sesion activa, 404 si negocio/usuario/variante no existe
     */
    @Override
    @Transactional
    public SesionDto crear(CrearSesionDto dto) {
        if (sesionRepositorio.existsByNegocio_NegocioIdAndEstatus(dto.negocioId(), EstatusSesion.EN_CURSO)) {
            throw new ComalaExcepcion(
                    Constantes.ERR_SESION_YA_ACTIVA,
                    "Ya existe una sesion activa para este negocio.",
                    Constantes.HTTP_409_CONFLICT
            );
        }

        Negocio negocio = negocioRepositorio.findById(dto.negocioId())
                .orElseThrow(() -> new ComalaExcepcion(
                        Constantes.ERR_NEGOCIO_NO_ENCONTRADO,
                        "Negocio no encontrado.",
                        Constantes.HTTP_404_NOT_FOUND
                ));

        Usuario creador = usuarioRepositorio.findById(dto.creadaPorId())
                .orElseThrow(() -> new ComalaExcepcion(
                        Constantes.ERR_USUARIO_NO_ENCONTRADO,
                        "Usuario no encontrado.",
                        Constantes.HTTP_404_NOT_FOUND
                ));

        Sesion sesion = new Sesion();
        sesion.setNegocio(negocio);
        sesion.setCreadaPor(creador);
        sesion.setNombre(dto.nombre());
        sesion.setFecha(LocalDate.now());
        sesion.setEstatus(EstatusSesion.EN_CURSO);
        sesion.setTotalCalculado(BigDecimal.ZERO);
        sesion = sesionRepositorio.save(sesion);

        for (var productoEntrada : dto.productos()) {
            VarianteProducto variante = varianteRepositorio.findById(productoEntrada.varianteId())
                    .orElseThrow(() -> new ComalaExcepcion(
                            Constantes.ERR_VARIANTE_NO_ENCONTRADA,
                            "Variante no encontrada: " + productoEntrada.varianteId(),
                            Constantes.HTTP_404_NOT_FOUND
                    ));

            ProductoSesion ps = new ProductoSesion();
            ps.setSesion(sesion);
            ps.setVariante(variante);
            ps.setPrecioSesion(productoEntrada.precioSesion());
            ps.setDisponible(true);
            productoSesionRepositorio.save(ps);
        }

        log.info("[SESION] Sesion creada: id={}, nombre='{}', negocioId={}, productos={}",
                sesion.getSesionId(), sesion.getNombre(), dto.negocioId(), dto.productos().size());

        return toDto(sesion);
    }

    /**
     * Finaliza la sesion activa. Valida que no haya pedidos en estatus abierto.
     *
     * @param sesionId  identificador de la sesion
     * @param negocioId identificador del negocio (validacion)
     * @return sesion finalizada
     * @throws ComalaExcepcion 404 si no existe, 403 si no pertenece al negocio, 409 si ya finalizada, 422 si hay pedidos abiertos
     */
    @Override
    @Transactional
    public SesionDto finalizar(UUID sesionId, UUID negocioId) {
        Sesion sesion = buscarYValidar(sesionId, negocioId);

        if (sesion.getEstatus() == EstatusSesion.FINALIZADA) {
            throw new ComalaExcepcion(
                    Constantes.ERR_SESION_YA_FINALIZADA,
                    "La sesion ya fue finalizada.",
                    Constantes.HTTP_409_CONFLICT
            );
        }

        List<EstatusPedido> estatusAbiertos = List.of(
                EstatusPedido.RECIEN_PEDIDO, EstatusPedido.PREPARANDO, EstatusPedido.EMBOLSANDO
        );
        boolean hayPedidosAbiertos = !pedidoRepositorio
                .findBySesion_SesionIdAndEstatusIn(sesionId, estatusAbiertos)
                .isEmpty();

        if (hayPedidosAbiertos) {
            throw new ComalaExcepcion(
                    Constantes.ERR_SESION_YA_ACTIVA,
                    "No se puede finalizar la sesion con pedidos abiertos.",
                    Constantes.HTTP_422_UNPROCESSABLE
            );
        }

        sesion.setEstatus(EstatusSesion.FINALIZADA);
        sesion = sesionRepositorio.save(sesion);

        log.info("[SESION] Sesion finalizada: id={}, nombre='{}', total={}",
                sesion.getSesionId(), sesion.getNombre(), sesion.getTotalCalculado());

        return toDto(sesion);
    }

    /**
     * Obtiene una sesion por ID, validando pertenencia al negocio.
     *
     * @param sesionId  identificador de la sesion
     * @param negocioId identificador del negocio
     * @return datos de la sesion
     * @throws ComalaExcepcion 404 si no existe, 403 si no pertenece al negocio
     */
    @Override
    @Transactional(readOnly = true)
    public SesionDto obtenerPorId(UUID sesionId, UUID negocioId) {
        return toDto(buscarYValidar(sesionId, negocioId));
    }

    /**
     * Lista sesiones finalizadas del negocio, ordenadas por fecha de creacion descendente.
     *
     * @param negocioId identificador del negocio
     * @return lista de sesiones finalizadas
     */
    @Override
    @Transactional(readOnly = true)
    public List<SesionDto> listarFinalizadas(UUID negocioId) {
        return sesionRepositorio.findByNegocio_NegocioIdOrderByCreatedAtDesc(negocioId).stream()
                .filter(s -> s.getEstatus() == EstatusSesion.FINALIZADA)
                .map(this::toDto)
                .toList();
    }

    /**
     * Lista los productos disponibles en una sesion con sus precios ajustados.
     *
     * @param sesionId  identificador de la sesion
     * @param negocioId identificador del negocio (validacion)
     * @return lista de productos de la sesion
     * @throws ComalaExcepcion 404 si la sesion no existe, 403 si no pertenece al negocio
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductoSesionDto> listarProductosSesion(UUID sesionId, UUID negocioId) {
        buscarYValidar(sesionId, negocioId);
        return productoSesionRepositorio.findBySesion_SesionId(sesionId).stream()
                .map(this::toProductoSesionDto)
                .toList();
    }

    private Sesion buscarYValidar(UUID sesionId, UUID negocioId) {
        Sesion sesion = sesionRepositorio.findById(sesionId)
                .orElseThrow(() -> new ComalaExcepcion(
                        Constantes.ERR_SESION_NO_ENCONTRADA,
                        "Sesion no encontrada.",
                        Constantes.HTTP_404_NOT_FOUND
                ));

        if (!sesion.getNegocio().getNegocioId().equals(negocioId)) {
            throw new ComalaExcepcion(
                    Constantes.ERR_ACCESO_DENEGADO,
                    "No tienes acceso a esta sesion.",
                    Constantes.HTTP_403_FORBIDDEN
            );
        }

        return sesion;
    }

    private SesionDto toDto(Sesion s) {
        return new SesionDto(
                s.getSesionId(),
                s.getNombre(),
                s.getFecha(),
                s.getEstatus(),
                s.getTotalCalculado(),
                s.getCreatedAt()
        );
    }

    private ProductoSesionDto toProductoSesionDto(ProductoSesion ps) {
        return new ProductoSesionDto(
                ps.getProductoSesionId(),
                ps.getVariante().getVarianteId(),
                ps.getVariante().getNombreVariante(),
                ps.getVariante().getProducto().getNombre(),
                ps.getPrecioSesion(),
                ps.isDisponible()
        );
    }
}
