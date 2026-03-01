package dev.jeatog.comala.api.models;

import java.time.Instant;

public record ErrorRes(
        String codigo,
        String mensaje,
        Instant timestamp
) {
    public static ErrorRes de(String codigo, String mensaje) {
        return new ErrorRes(codigo, mensaje, Instant.now());
    }
}
