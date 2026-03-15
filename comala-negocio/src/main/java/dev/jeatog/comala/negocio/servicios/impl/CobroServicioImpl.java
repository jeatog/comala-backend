package dev.jeatog.comala.negocio.servicios.impl;

import dev.jeatog.comala.negocio.constantes.Constantes;
import dev.jeatog.comala.negocio.dto.cobro.CobrarDto;
import dev.jeatog.comala.negocio.dto.cobro.CobroDto;
import dev.jeatog.comala.negocio.excepcion.ComalaExcepcion;
import dev.jeatog.comala.negocio.servicios.CobroServicio;
import dev.jeatog.comala.persistencia.entidades.Pedido;
import dev.jeatog.comala.persistencia.enums.MetodoPago;
import dev.jeatog.comala.persistencia.repositorios.PedidoRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CobroServicioImpl implements CobroServicio {

    private final PedidoRepositorio pedidoRepositorio;

    /**
     * Lista los pedidos fiados del negocio segun filtro: VENCIDO, PROXIMO o COBRADO.
     *
     * @param negocioId identificador del negocio
     * @param filtro    VENCIDO, PROXIMO o COBRADO (null devuelve pendientes: vencidos + proximos)
     * @return lista de cobros
     */
    @Override
    @Transactional(readOnly = true)
    public List<CobroDto> listarFiados(UUID negocioId, String filtro) {
        if ("COBRADO".equalsIgnoreCase(filtro)) {
            return pedidoRepositorio
                    .findBySesion_Negocio_NegocioIdAndMetodoPagoAndPagadoTrueOrderByFechaPagoRealDesc(
                            negocioId, MetodoPago.FIADO)
                    .stream()
                    .map(this::toDto)
                    .toList();
        }

        List<Pedido> pendientes = pedidoRepositorio
                .findBySesion_Negocio_NegocioIdAndMetodoPagoAndPagadoFalseOrderByFechaCompromisoAsc(
                        negocioId, MetodoPago.FIADO);

        LocalDate hoy = LocalDate.now();

        if ("VENCIDO".equalsIgnoreCase(filtro)) {
            return pendientes.stream()
                    .filter(p -> p.getFechaCompromiso() != null && p.getFechaCompromiso().isBefore(hoy))
                    .map(this::toDto)
                    .toList();
        }

        if ("PROXIMO".equalsIgnoreCase(filtro)) {
            return pendientes.stream()
                    .filter(p -> p.getFechaCompromiso() == null || !p.getFechaCompromiso().isBefore(hoy))
                    .map(this::toDto)
                    .toList();
        }

        // Sin filtro: todos los pendientes
        return pendientes.stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Marca un pedido fiado como cobrado. Cambia metodo de pago al real y registra fecha de cobro.
     *
     * @param dto datos del cobro
     * @return cobro actualizado
     * @throws ComalaExcepcion 404 si no existe, 403 si no pertenece, 409 si ya pagado, 400 si no es fiado o metodo invalido
     */
    @Override
    @Transactional
    public CobroDto cobrar(CobrarDto dto) {
        Pedido pedido = pedidoRepositorio.findById(dto.pedidoId())
                .orElseThrow(() -> new ComalaExcepcion(
                        Constantes.ERR_PEDIDO_NO_ENCONTRADO,
                        "Pedido no encontrado.",
                        Constantes.HTTP_404_NOT_FOUND
                ));

        if (!pedido.getSesion().getNegocio().getNegocioId().equals(dto.negocioId())) {
            throw new ComalaExcepcion(
                    Constantes.ERR_ACCESO_DENEGADO,
                    "No tienes acceso a este pedido.",
                    Constantes.HTTP_403_FORBIDDEN
            );
        }

        if (pedido.getMetodoPago() != MetodoPago.FIADO) {
            throw new ComalaExcepcion(
                    Constantes.ERR_COBRO_NO_ES_FIADO,
                    "Solo se pueden cobrar pedidos con metodo FIADO.",
                    Constantes.HTTP_400_BAD_REQUEST
            );
        }

        if (pedido.isPagado()) {
            throw new ComalaExcepcion(
                    Constantes.ERR_COBRO_YA_PAGADO,
                    "Este pedido ya fue cobrado.",
                    Constantes.HTTP_409_CONFLICT
            );
        }

        if (dto.metodoPagoReal() == MetodoPago.FIADO) {
            throw new ComalaExcepcion(
                    Constantes.ERR_COBRO_METODO_INVALIDO,
                    "El metodo de pago real no puede ser FIADO.",
                    Constantes.HTTP_400_BAD_REQUEST
            );
        }

        pedido.setMetodoPago(dto.metodoPagoReal());
        pedido.setPagado(true);
        pedido.setFechaPagoReal(Instant.now());
        pedido = pedidoRepositorio.save(pedido);

        log.info("[COBRO] Pedido cobrado: id={}, metodoPagoReal={}, cliente='{}'",
                pedido.getPedidoId(), dto.metodoPagoReal(), pedido.getNombreCliente());

        return toDto(pedido);
    }

    private CobroDto toDto(Pedido p) {
        return new CobroDto(
                p.getPedidoId(),
                p.getSesion().getSesionId(),
                p.getSesion().getNombre(),
                p.getNombreCliente(),
                p.getDireccion(),
                p.getTotal(),
                p.getFechaCompromiso(),
                p.getMetodoPago(),
                p.isPagado(),
                p.getFechaPagoReal(),
                p.getCreatedAt()
        );
    }
}
