package dev.jeatog.comala.negocio.excepcion;

import org.springframework.http.HttpStatus;

public class ComalaExcepcion extends RuntimeException {

    private final String codigo;
    private final HttpStatus status;

    public ComalaExcepcion(String codigo, String mensaje, HttpStatus status) {
        super(mensaje);
        this.codigo = codigo;
        this.status = status;
    }

    public String getCodigo() {
        return codigo;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
