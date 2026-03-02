package dev.jeatog.comala.api.aspecto;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jeatog.comala.negocio.excepcion.ComalaExcepcion;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
public class ControllerLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ControllerLoggingAspect.class);

    private final ObjectMapper objectMapper;

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object loguearPeticionRespuesta(ProceedingJoinPoint pjp) throws Throwable {
        String accion = pjp.getSignature().getName().toUpperCase();
        Object[] todosArgs = pjp.getArgs();

        MultipartFile archivo = Arrays.stream(todosArgs)
                .filter(MultipartFile.class::isInstance)
                .map(MultipartFile.class::cast)
                .findFirst()
                .orElse(null);

        List<Object> argsBody = Arrays.stream(todosArgs)
                .filter(arg -> arg != null
                        && !(arg instanceof Authentication)
                        && !(arg instanceof MultipartFile))
                .toList();

        if (archivo != null) {
            log.info("[{}] Peticion: archivo={}, size={} bytes", accion,
                    archivo.getOriginalFilename(), archivo.getSize());
        } else if (!argsBody.isEmpty()) {
            log.info("[{}] Peticion: {}", accion,
                    serializar(argsBody.size() == 1 ? argsBody.getFirst() : argsBody));
        } else {
            log.info("[{}] Peticion", accion);
        }

        try {
            Object resultado = pjp.proceed();

            if (resultado instanceof ResponseEntity<?> res) {
                int status = res.getStatusCode().value();
                if (res.getBody() != null) {
                    log.info("[{}] Respuesta {}: {}", accion, status, serializar(res.getBody()));
                } else {
                    log.info("[{}] Respuesta {}", accion, status);
                }
            }

            return resultado;

        } catch (ComalaExcepcion e) {
            log.warn("[{}] Error {}: [{}] {}", accion, e.getHttpStatus(), e.getCodigo(), e.getMessage());
            throw e;
        } catch (Throwable t) {
            log.error("[{}] Error inesperado: {}", accion, t.getMessage());
            throw t;
        }
    }

    private String serializar(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}
