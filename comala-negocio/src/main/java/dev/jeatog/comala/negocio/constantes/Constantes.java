package dev.jeatog.comala.negocio.constantes;

public final class Constantes {

    private Constantes() {}

    // Códigos de error
    public static final String ERR_SESION_YA_ACTIVA      = "SESION_YA_ACTIVA";
    public static final String ERR_SESION_NO_ENCONTRADA  = "SESION_NO_ENCONTRADA";
    public static final String ERR_SESION_YA_FINALIZADA  = "SESION_YA_FINALIZADA";

    public static final String ERR_PEDIDO_NO_ENCONTRADO  = "PEDIDO_NO_ENCONTRADO";
    public static final String ERR_PEDIDO_CONFLICTO      = "PEDIDO_CONFLICTO";
    public static final String ERR_PEDIDO_SIN_LINEAS     = "PEDIDO_SIN_LINEAS";

    public static final String ERR_USUARIO_NO_ENCONTRADO    = "USUARIO_NO_ENCONTRADO";
    public static final String ERR_USUARIO_CONFLICTO        = "USUARIO_CONFLICTO";
    public static final String ERR_CREDENCIALES_INVALIDAS   = "CREDENCIALES_INVALIDAS";
    public static final String ERR_ACCESO_DENEGADO          = "ACCESO_DENEGADO";
    public static final String ERR_TOKEN_INVALIDO           = "TOKEN_INVALIDO";

    public static final String ERR_NEGOCIO_NO_ENCONTRADO = "NEGOCIO_NO_ENCONTRADO";
    public static final String ERR_NEGOCIO_NO_PERTENECE  = "NEGOCIO_NO_PERTENECE";

    public static final String ERR_PRODUCTO_NO_ENCONTRADO   = "PRODUCTO_NO_ENCONTRADO";
    public static final String ERR_VARIANTE_NO_ENCONTRADA   = "VARIANTE_NO_ENCONTRADA";
    public static final String ERR_VARIANTE_NO_DISPONIBLE   = "VARIANTE_NO_DISPONIBLE";
    public static final String ERR_CATEGORIA_NO_ENCONTRADA  = "CATEGORIA_NO_ENCONTRADA";

    // Valores de negocio
    public static final String CLAIM_NEGOCIO_ACTIVO_ID = "negocio_activo_id";
    public static final String CLAIM_ROL               = "rol";

    // Estatus HTTP usados al lanzar ComalaExcepcion
    public static final int HTTP_400_BAD_REQUEST   = 400;
    public static final int HTTP_401_UNAUTHORIZED  = 401;
    public static final int HTTP_403_FORBIDDEN     = 403;
    public static final int HTTP_404_NOT_FOUND     = 404;
    public static final int HTTP_409_CONFLICT      = 409;
    public static final int HTTP_422_UNPROCESSABLE = 422;
}
