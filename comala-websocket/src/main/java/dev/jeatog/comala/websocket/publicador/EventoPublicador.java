package dev.jeatog.comala.websocket.publicador;

import dev.jeatog.comala.websocket.models.PedidoEventoMsg;
import dev.jeatog.comala.websocket.models.SesionEventoMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventoPublicador {

    private final SimpMessagingTemplate messagingTemplate;

    public void publicarEventoSesion(UUID negocioId, SesionEventoMsg evento) {
        String destino = "/ws/negocio/" + negocioId;
        log.info("[WS] Publicando {} en {} (sesionId={})", evento.tipo(), destino, evento.sesionId());
        messagingTemplate.convertAndSend(destino, evento);
    }

    public void publicarEventoPedido(UUID sesionId, PedidoEventoMsg evento) {
        String destino = "/ws/sesion/" + sesionId;
        log.info("[WS] Publicando {} en {} (pedidoId={})", evento.tipo(), destino, evento.pedidoId());
        messagingTemplate.convertAndSend(destino, evento);
    }
}
