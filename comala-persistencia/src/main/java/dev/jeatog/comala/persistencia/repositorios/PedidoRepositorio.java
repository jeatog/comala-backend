package dev.jeatog.comala.persistencia.repositorios;

import dev.jeatog.comala.persistencia.entidades.Pedido;
import dev.jeatog.comala.persistencia.enums.EstatusPedido;
import dev.jeatog.comala.persistencia.enums.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PedidoRepositorio extends JpaRepository<Pedido, UUID> {

    List<Pedido> findBySesion_SesionIdOrderByCreatedAtAsc(UUID sesionId);

    List<Pedido> findBySesion_SesionIdAndEstatusNotInOrderByCreatedAtAsc(
            UUID sesionId, List<EstatusPedido> estatusExcluidos
    );

    List<Pedido> findBySesion_Negocio_NegocioIdAndMetodoPagoAndPagadoFalse(
            UUID negocioId, MetodoPago metodoPago
    );
}
