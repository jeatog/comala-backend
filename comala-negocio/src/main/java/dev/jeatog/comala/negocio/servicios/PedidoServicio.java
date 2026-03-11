package dev.jeatog.comala.negocio.servicios;

import dev.jeatog.comala.negocio.dto.pedido.CrearPedidoDto;
import dev.jeatog.comala.negocio.dto.pedido.EditarPedidoDto;
import dev.jeatog.comala.negocio.dto.pedido.PedidoDto;
import dev.jeatog.comala.persistencia.enums.EstatusPedido;

import java.util.List;
import java.util.UUID;

public interface PedidoServicio {

    PedidoDto crear(CrearPedidoDto dto, UUID negocioId);
    PedidoDto editar(EditarPedidoDto dto);
    PedidoDto cambiarEstatus(UUID pedidoId, UUID negocioId, EstatusPedido nuevoEstatus);
    UUID eliminar(UUID pedidoId, UUID negocioId);
    PedidoDto obtenerPorId(UUID pedidoId, UUID negocioId);
    List<PedidoDto> listarPorSesion(UUID sesionId, UUID negocioId);

}
