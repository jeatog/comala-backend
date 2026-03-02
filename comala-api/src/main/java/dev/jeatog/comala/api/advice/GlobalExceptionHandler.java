package dev.jeatog.comala.api.advice;

import dev.jeatog.comala.api.models.ErrorRes;
import dev.jeatog.comala.negocio.constantes.Constantes;
import dev.jeatog.comala.negocio.excepcion.ComalaExcepcion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ComalaExcepcion.class)
    public ResponseEntity<ErrorRes> manejarComalaExcepcion(ComalaExcepcion ex) {
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(ErrorRes.de(ex.getCodigo(), ex.getMessage()));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorRes> manejarConflictoPedido(ObjectOptimisticLockingFailureException ex) {
        log.warn("Conflicto de concurrencia en: {}", ex.getPersistentClassName());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorRes.de(Constantes.ERR_PEDIDO_CONFLICTO,
                        "El pedido fue modificado por otro usuario. Por favor recárgalo e intenta de nuevo."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRes> manejarValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("Datos de entrada inválidos");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorRes.de("VALIDACION_FALLIDA", mensaje));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRes> manejarExcepcionGenerica(Exception ex) {
        log.error("Error inesperado", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorRes.de("ERROR_INTERNO", "Ocurrió un error inesperado. Intenta de nuevo más tarde."));
    }
}
