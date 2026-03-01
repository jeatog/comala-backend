package dev.jeatog.comala.persistencia.repositorios;

import dev.jeatog.comala.persistencia.entidades.PedidoLinea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PedidoLineaRepositorio extends JpaRepository<PedidoLinea, UUID> {

    List<PedidoLinea> findByPedido_PedidoId(UUID pedidoId);
}
