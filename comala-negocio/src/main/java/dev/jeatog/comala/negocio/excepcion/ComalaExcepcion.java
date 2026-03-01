package dev.jeatog.comala.negocio.excepcion;

public class ComalaExcepcion extends RuntimeException {

    private final String codigo;
    private final int httpStatus;

    public ComalaExcepcion(String codigo, String mensaje, int httpStatus) {
        super(mensaje);
        this.codigo = codigo;
        this.httpStatus = httpStatus;
    }

    public String getCodigo() {
        return codigo;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
