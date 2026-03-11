package dev.jeatog.comala.negocio.servicios.impl;

import dev.jeatog.comala.negocio.constantes.Constantes;
import dev.jeatog.comala.negocio.dto.pedido.CrearPedidoDto;
import dev.jeatog.comala.negocio.dto.pedido.EditarPedidoDto;
import dev.jeatog.comala.negocio.dto.pedido.PedidoDto;
import dev.jeatog.comala.negocio.dto.pedido.PedidoLineaDto;
import dev.jeatog.comala.negocio.dto.pedido.PedidoLineaEntradaDto;
import dev.jeatog.comala.negocio.excepcion.ComalaExcepcion;
import dev.jeatog.comala.negocio.servicios.PedidoServicio;
import dev.jeatog.comala.persistencia.entidades.Pedido;
import dev.jeatog.comala.persistencia.entidades.PedidoLinea;
import dev.jeatog.comala.persistencia.entidades.ProductoSesion;
import dev.jeatog.comala.persistencia.entidades.Sesion;
import dev.jeatog.comala.persistencia.enums.EstatusPedido;
import dev.jeatog.comala.persistencia.enums.EstatusSesion;
import dev.jeatog.comala.persistencia.enums.MetodoPago;
import dev.jeatog.comala.persistencia.repositorios.PedidoRepositorio;
import dev.jeatog.comala.persistencia.repositorios.ProductoSesionRepositorio;
import dev.jeatog.comala.persistencia.repositorios.SesionRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoServicioImpl implements PedidoServicio {

    private final PedidoRepositorio pedidoRepositorio;
    private final SesionRepositorio sesionRepositorio;
    private final ProductoSesionRepositorio productoSesionRepositorio;

    /**
     * Crea un pedido nuevo en la sesion indicada. Calcula totales y congela precios.
     *
     * @param dto       datos del pedido a crear
     * @param negocioId identificador del negocio (validacion)
     * @return pedido creado con lineas
     * @throws ComalaExcepcion 404 si sesion no existe, 403 si no pertenece al negocio,
     *                         409 si la sesion esta finalizada, 400 si no tiene lineas,
     *                         404 si variante no disponible en sesion
     */
    @Override
    @Transactional
    public PedidoDto crear(CrearPedidoDto dto, UUID negocioId) {
        Sesion sesion = buscarSesionYValidar(dto.sesionId(), negocioId);

        if (sesion.getEstatus() != EstatusSesion.EN_CURSO) {
            throw new ComalaExcepcion(
                    Constantes.ERR_SESION_YA_FINALIZADA,
                    "No se pueden crear pedidos en una sesion finalizada.",
                    Constantes.HTTP_409_CONFLICT
            );
        }

        if (dto.lineas() == null || dto.lineas().isEmpty()) {
            throw new ComalaExcepcion(
                    Constantes.ERR_PEDIDO_SIN_LINEAS,
                    "El pedido debe tener al menos una linea.",
                    Constantes.HTTP_400_BAD_REQUEST
            );
        }

        validarFiadoConFechaCompromiso(dto.metodoPago(), dto.fechaCompromiso());

        Map<UUID, ProductoSesion> productosPorVariante = obtenerProductosSesion(dto.sesionId());

        Pedido pedido = new Pedido();
        pedido.setSesion(sesion);
        pedido.setNombreCliente(dto.nombreCliente());
        pedido.setDireccion(dto.direccion());
        pedido.setEstatus(EstatusPedido.RECIEN_PEDIDO);
        pedido.setMetodoPago(dto.metodoPago());
        pedido.setMetodoSolicitud(dto.metodoSolicitud());
        pedido.setTipoEnvio(dto.tipoEnvio());
        pedido.setFechaCompromiso(dto.fechaCompromiso());
        pedido.setPagado(false);

        List<PedidoLinea> lineas = construirLineas(dto.lineas(), pedido, productosPorVariante);
        pedido.setLineas(lineas);
        pedido.setTotal(calcularTotal(lineas));

        pedido = pedidoRepositorio.save(pedido);
        actualizarTotalSesion(sesion);

        log.info("[PEDIDO] Pedido creado: id={}, sesionId={}, cliente='{}', total={}, lineas={}",
                pedido.getPedidoId(), dto.sesionId(), dto.nombreCliente(),
                pedido.getTotal(), lineas.size());

        return toDto(pedido);
    }

    /**
     * Edita un pedido existente (lineas, cliente, entrega, pago). Recalcula totales.
     *
     * @param dto datos actualizados del pedido
     * @return pedido actualizado
     * @throws ComalaExcepcion 404 si no existe, 403 si no pertenece al negocio, 400 si sin lineas
     */
    @Override
    @Transactional
    public PedidoDto editar(EditarPedidoDto dto) {
        Pedido pedido = buscarPedidoYValidar(dto.pedidoId(), dto.negocioId());

        if (dto.lineas() == null || dto.lineas().isEmpty()) {
            throw new ComalaExcepcion(
                    Constantes.ERR_PEDIDO_SIN_LINEAS,
                    "El pedido debe tener al menos una linea.",
                    Constantes.HTTP_400_BAD_REQUEST
            );
        }

        validarFiadoConFechaCompromiso(dto.metodoPago(), dto.fechaCompromiso());

        UUID sesionId = pedido.getSesion().getSesionId();
        Map<UUID, ProductoSesion> productosPorVariante = obtenerProductosSesion(sesionId);

        pedido.setNombreCliente(dto.nombreCliente());
        pedido.setDireccion(dto.direccion());
        pedido.setMetodoPago(dto.metodoPago());
        pedido.setMetodoSolicitud(dto.metodoSolicitud());
        pedido.setTipoEnvio(dto.tipoEnvio());
        pedido.setFechaCompromiso(dto.fechaCompromiso());

        pedido.getLineas().clear();
        List<PedidoLinea> nuevasLineas = construirLineas(dto.lineas(), pedido, productosPorVariante);
        pedido.getLineas().addAll(nuevasLineas);
        pedido.setTotal(calcularTotal(nuevasLineas));

        pedido = pedidoRepositorio.save(pedido);
        actualizarTotalSesion(pedido.getSesion());

        log.info("[PEDIDO] Pedido editado: id={}, total={}", pedido.getPedidoId(), pedido.getTotal());

        return toDto(pedido);
    }

    /**
     * Cambia el estatus de un pedido.
     *
     * @param pedidoId     identificador del pedido
     * @param negocioId    identificador del negocio (validacion)
     * @param nuevoEstatus nuevo estatus
     * @return pedido actualizado
     * @throws ComalaExcepcion 404 si no existe, 403 si no pertenece al negocio
     */
    @Override
    @Transactional
    public PedidoDto cambiarEstatus(UUID pedidoId, UUID negocioId, EstatusPedido nuevoEstatus) {
        Pedido pedido = buscarPedidoYValidar(pedidoId, negocioId);

        EstatusPedido estatusAnterior = pedido.getEstatus();
        pedido.setEstatus(nuevoEstatus);

        if (nuevoEstatus == EstatusPedido.ENTREGADO && pedido.getMetodoPago() != MetodoPago.FIADO) {
            pedido.setPagado(true);
        }

        pedido = pedidoRepositorio.save(pedido);

        log.info("[PEDIDO] Estatus cambiado: id={}, {} -> {}", pedidoId, estatusAnterior, nuevoEstatus);

        return toDto(pedido);
    }

    /**
     * Elimina un pedido (borrado fisico). Retorna el sesionId para notificacion.
     *
     * @param pedidoId  identificador del pedido
     * @param negocioId identificador del negocio (validacion)
     * @return sesionId al que pertenecia el pedido
     * @throws ComalaExcepcion 404 si no existe, 403 si no pertenece al negocio
     */
    @Override
    @Transactional
    public UUID eliminar(UUID pedidoId, UUID negocioId) {
        Pedido pedido = buscarPedidoYValidar(pedidoId, negocioId);
        UUID sesionId = pedido.getSesion().getSesionId();
        Sesion sesion = pedido.getSesion();

        pedidoRepositorio.delete(pedido);
        actualizarTotalSesion(sesion);

        log.info("[PEDIDO] Pedido eliminado: id={}, sesionId={}", pedidoId, sesionId);

        return sesionId;
    }

    /**
     * Obtiene un pedido por ID con sus lineas.
     *
     * @param pedidoId  identificador del pedido
     * @param negocioId identificador del negocio (validacion)
     * @return pedido con lineas
     * @throws ComalaExcepcion 404 si no existe, 403 si no pertenece al negocio
     */
    @Override
    @Transactional(readOnly = true)
    public PedidoDto obtenerPorId(UUID pedidoId, UUID negocioId) {
        return toDto(buscarPedidoYValidar(pedidoId, negocioId));
    }

    /**
     * Lista todos los pedidos de una sesion, ordenados por fecha de creacion.
     *
     * @param sesionId  identificador de la sesion
     * @param negocioId identificador del negocio (validacion)
     * @return lista de pedidos con lineas
     * @throws ComalaExcepcion 404 si la sesion no existe, 403 si no pertenece al negocio
     */
    @Override
    @Transactional(readOnly = true)
    public List<PedidoDto> listarPorSesion(UUID sesionId, UUID negocioId) {
        buscarSesionYValidar(sesionId, negocioId);
        return pedidoRepositorio.findBySesion_SesionIdOrderByCreatedAtAsc(sesionId).stream()
                .map(this::toDto)
                .toList();
    }

    // Métodos autilixares

    private Sesion buscarSesionYValidar(UUID sesionId, UUID negocioId) {
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

    private Pedido buscarPedidoYValidar(UUID pedidoId, UUID negocioId) {
        Pedido pedido = pedidoRepositorio.findById(pedidoId)
                .orElseThrow(() -> new ComalaExcepcion(
                        Constantes.ERR_PEDIDO_NO_ENCONTRADO,
                        "Pedido no encontrado.",
                        Constantes.HTTP_404_NOT_FOUND
                ));

        if (!pedido.getSesion().getNegocio().getNegocioId().equals(negocioId)) {
            throw new ComalaExcepcion(
                    Constantes.ERR_ACCESO_DENEGADO,
                    "No tienes acceso a este pedido.",
                    Constantes.HTTP_403_FORBIDDEN
            );
        }

        return pedido;
    }

    private void validarFiadoConFechaCompromiso(MetodoPago metodoPago, java.time.LocalDate fechaCompromiso) {
        if (metodoPago == MetodoPago.FIADO && fechaCompromiso == null) {
            throw new ComalaExcepcion(
                    Constantes.ERR_PEDIDO_SIN_LINEAS,
                    "Metodo FIADO requiere fecha de compromiso.",
                    Constantes.HTTP_400_BAD_REQUEST
            );
        }
    }

    private Map<UUID, ProductoSesion> obtenerProductosSesion(UUID sesionId) {
        return productoSesionRepositorio.findDisponiblesPorSesion(sesionId).stream()
                .collect(Collectors.toMap(
                        ps -> ps.getVariante().getVarianteId(),
                        Function.identity()
                ));
    }

    private List<PedidoLinea> construirLineas(
            List<PedidoLineaEntradaDto> entradasLineas,
            Pedido pedido,
            Map<UUID, ProductoSesion> productosPorVariante
    ) {
        List<PedidoLinea> lineas = new ArrayList<>();
        for (var entrada : entradasLineas) {
            ProductoSesion ps = productosPorVariante.get(entrada.varianteId());
            if (ps == null) {
                throw new ComalaExcepcion(
                        Constantes.ERR_VARIANTE_NO_DISPONIBLE,
                        "La variante " + entrada.varianteId() + " no esta disponible en esta sesion.",
                        Constantes.HTTP_404_NOT_FOUND
                );
            }

            BigDecimal precioUnitario = ps.getPrecioSesion();
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(entrada.cantidad()));

            PedidoLinea linea = new PedidoLinea();
            linea.setPedido(pedido);
            linea.setVariante(ps.getVariante());
            linea.setCantidad(entrada.cantidad());
            linea.setPrecioUnitario(precioUnitario);
            linea.setSubtotal(subtotal);
            linea.setNotas(entrada.notas());
            lineas.add(linea);
        }
        return lineas;
    }

    private BigDecimal calcularTotal(List<PedidoLinea> lineas) {
        return lineas.stream()
                .map(PedidoLinea::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void actualizarTotalSesion(Sesion sesion) {
        BigDecimal total = pedidoRepositorio
                .findBySesion_SesionIdOrderByCreatedAtAsc(sesion.getSesionId()).stream()
                .filter(p -> p.getEstatus() != EstatusPedido.CANCELADO
                        && p.getEstatus() != EstatusPedido.DEVUELTO_CANCELADO)
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        sesion.setTotalCalculado(total);
        sesionRepositorio.save(sesion);
    }

    private PedidoDto toDto(Pedido p) {
        List<PedidoLineaDto> lineasDto = p.getLineas() != null
                ? p.getLineas().stream().map(this::toLineaDto).toList()
                : List.of();

        return new PedidoDto(
                p.getPedidoId(),
                p.getSesion().getSesionId(),
                p.getNombreCliente(),
                p.getDireccion(),
                p.getEstatus(),
                p.getMetodoPago(),
                p.getMetodoSolicitud(),
                p.getTipoEnvio(),
                p.getTotal(),
                p.isPagado(),
                p.getFechaCompromiso(),
                p.getCreatedAt(),
                lineasDto
        );
    }

    private PedidoLineaDto toLineaDto(PedidoLinea l) {
        return new PedidoLineaDto(
                l.getLineaId(),
                l.getVariante().getVarianteId(),
                l.getVariante().getNombreVariante(),
                l.getVariante().getProducto().getNombre(),
                l.getCantidad(),
                l.getPrecioUnitario(),
                l.getSubtotal(),
                l.getNotas()
        );
    }
}
