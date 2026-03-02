package dev.jeatog.comala.api.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jeatog.comala.api.models.ErrorRes;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Maneja peticiones sin autenticacion valida (401).
 */
@Component
@RequiredArgsConstructor
public class SeguridadEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(SeguridadEntryPoint.class);

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.warn("[SEGURIDAD] 401 No autenticado: {} {} — {}",
                request.getMethod(), request.getRequestURI(), authException.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(),
                ErrorRes.de("NO_AUTENTICADO", "Autenticacion requerida. Incluye un token JWT valido."));
    }
}
