package dev.jeatog.comala.websocket.presencia;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class PresenciaServicio {

    private static final Pattern PATRON_SESION = Pattern.compile("^/ws/sesion/([0-9a-fA-F-]+)$");

    // sesionId -> Set de emails conectados
    private final Map<UUID, Set<String>> presenciaPorSesion = new ConcurrentHashMap<>();

    // stompSessionId -> (email, sesionId) para limpiar al desconectar
    private final Map<String, DatosSuscripcion> suscripciones = new ConcurrentHashMap<>();

    public Set<String> obtenerConectados(UUID sesionId) {
        return presenciaPorSesion.getOrDefault(sesionId, Set.of());
    }

    @EventListener
    public void alSuscribir(SessionSubscribeEvent evento) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(evento.getMessage());
        String destino = accessor.getDestination();
        if (destino == null) return;

        Matcher matcher = PATRON_SESION.matcher(destino);
        if (!matcher.matches()) return;

        UUID sesionId = UUID.fromString(matcher.group(1));
        String email = obtenerEmail(accessor);
        if (email == null) return;

        String stompSessionId = accessor.getSessionId();
        presenciaPorSesion
                .computeIfAbsent(sesionId, k -> ConcurrentHashMap.newKeySet())
                .add(email);
        suscripciones.put(stompSessionId, new DatosSuscripcion(email, sesionId));

        log.info("[PRESENCIA] {} se conecto a sesion {}", email, sesionId);
    }

    @EventListener
    public void alDesuscribir(SessionUnsubscribeEvent evento) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(evento.getMessage());
        limpiar(accessor.getSessionId());
    }

    @EventListener
    public void alDesconectar(SessionDisconnectEvent evento) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(evento.getMessage());
        limpiar(accessor.getSessionId());
    }

    private void limpiar(String stompSessionId) {
        DatosSuscripcion datos = suscripciones.remove(stompSessionId);
        if (datos == null) return;

        Set<String> conectados = presenciaPorSesion.get(datos.sesionId());
        if (conectados != null) {
            conectados.remove(datos.email());
            if (conectados.isEmpty()) {
                presenciaPorSesion.remove(datos.sesionId());
            }
        }

        log.info("[PRESENCIA] {} se desconecto de sesion {}", datos.email(), datos.sesionId());
    }

    private String obtenerEmail(StompHeaderAccessor accessor) {
        Map<String, Object> attrs = accessor.getSessionAttributes();
        return attrs != null ? (String) attrs.get("email") : null;
    }

    private record DatosSuscripcion(String email, UUID sesionId) {}
}
