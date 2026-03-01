package dev.jeatog.comala.websocket.models;

import java.util.UUID;

public record SesionEventoMsg(
        TipoEventoSesion tipo,
        UUID sesionId,
        String nombreSesion
) {
    public enum TipoEventoSesion {
        SESION_CREADA,
        SESION_FINALIZADA
    }
}
