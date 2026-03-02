package dev.jeatog.comala.api.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jeatog.comala.api.models.ErrorRes;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Maneja peticiones autenticadas sin el rol requerido (403).
 */
@Component
@RequiredArgsConstructor
public class SeguridadAccesoDenegadoHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(SeguridadAccesoDenegadoHandler.class);

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String identidad = (auth instanceof PrincipalComala p)
                ? p.getEmail() + " [" + p.getRol() + "]"
                : "desconocido";

        log.warn("[SEGURIDAD] 403 Acceso denegado: {} {} — usuario: {}",
                request.getMethod(), request.getRequestURI(), identidad);

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        try {
            objectMapper.writeValue(response.getWriter(),
                    ErrorRes.de("ACCESO_DENEGADO", "No tienes permisos para realizar esta accion."));
        } catch (IOException e) {
            log.error("[SEGURIDAD] Error al escribir respuesta 403: {}", e.getMessage());
        }
    }
}
