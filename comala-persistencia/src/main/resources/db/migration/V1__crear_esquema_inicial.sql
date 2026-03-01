-- ============================================================
-- V1 — Esquema inicial de Comala
-- ============================================================


-- ============================================================
-- Negocio
-- ============================================================
CREATE TABLE negocio (
    negocio_id  UUID        NOT NULL DEFAULT gen_random_uuid(),
    nombre      VARCHAR(255) NOT NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_negocio PRIMARY KEY (negocio_id)
);


-- ============================================================
-- Usuario — independiente del negocio; la relación va en negocio_usuario
-- ============================================================
CREATE TABLE usuario (
    usuario_id    UUID         NOT NULL DEFAULT gen_random_uuid(),
    nombre        VARCHAR(255)  NOT NULL,
    email         VARCHAR(255)  NOT NULL,
    password_hash VARCHAR(255)  NOT NULL,
    foto_url      VARCHAR(500),
    activo        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_usuario    PRIMARY KEY (usuario_id),
    CONSTRAINT uq_usuario_email UNIQUE (email)
);


-- ============================================================
-- NegocioUsuario — pertenencia y rol de un usuario en un negocio
-- Un usuario puede pertenecer a varios negocios con distintos roles.
-- ============================================================
CREATE TABLE negocio_usuario (
    negocio_usuario_id UUID        NOT NULL DEFAULT gen_random_uuid(),
    negocio_id         UUID        NOT NULL,
    usuario_id         UUID        NOT NULL,
    rol                VARCHAR(20)  NOT NULL,
    activo             BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_negocio_usuario   PRIMARY KEY (negocio_usuario_id),
    CONSTRAINT fk_nu_negocio        FOREIGN KEY (negocio_id)  REFERENCES negocio(negocio_id),
    CONSTRAINT fk_nu_usuario        FOREIGN KEY (usuario_id)  REFERENCES usuario(usuario_id),
    CONSTRAINT uq_negocio_usuario   UNIQUE (negocio_id, usuario_id),
    CONSTRAINT ck_nu_rol            CHECK (rol IN ('ADMIN', 'OPERADOR', 'LECTOR'))
);


-- ============================================================
-- Categoria — agrupador de productos, ordenable
-- ============================================================
CREATE TABLE categoria (
    categoria_id UUID         NOT NULL DEFAULT gen_random_uuid(),
    negocio_id   UUID         NOT NULL,
    nombre       VARCHAR(255)  NOT NULL,
    activa       BOOLEAN       NOT NULL DEFAULT TRUE,
    orden        INTEGER       NOT NULL DEFAULT 0,
    CONSTRAINT pk_categoria    PRIMARY KEY (categoria_id),
    CONSTRAINT fk_cat_negocio  FOREIGN KEY (negocio_id) REFERENCES negocio(negocio_id)
);


-- ============================================================
-- Producto — sin borrado físico (soft delete con activo)
-- ============================================================
CREATE TABLE producto (
    producto_id  UUID         NOT NULL DEFAULT gen_random_uuid(),
    negocio_id   UUID         NOT NULL,
    categoria_id UUID         NOT NULL,
    nombre       VARCHAR(255)  NOT NULL,
    imagen_url   VARCHAR(500),
    activo       BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_producto       PRIMARY KEY (producto_id),
    CONSTRAINT fk_prod_negocio   FOREIGN KEY (negocio_id)   REFERENCES negocio(negocio_id),
    CONSTRAINT fk_prod_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(categoria_id)
);


-- ============================================================
-- VarianteProducto — cada producto tiene al menos una variante
-- ============================================================
CREATE TABLE variante_producto (
    variante_id     UUID          NOT NULL DEFAULT gen_random_uuid(),
    producto_id     UUID          NOT NULL,
    nombre_variante VARCHAR(255)   NOT NULL,
    precio_base     NUMERIC(10, 2) NOT NULL,
    activo          BOOLEAN        NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_variante       PRIMARY KEY (variante_id),
    CONSTRAINT fk_var_producto   FOREIGN KEY (producto_id) REFERENCES producto(producto_id)
);


-- ============================================================
-- Sesion — solo puede existir 1 EN_CURSO por negocio
-- La restricción se implementa con un índice parcial único.
-- ============================================================
CREATE TABLE sesion (
    sesion_id        UUID           NOT NULL DEFAULT gen_random_uuid(),
    negocio_id       UUID           NOT NULL,
    nombre           VARCHAR(255)    NOT NULL,
    fecha            DATE            NOT NULL,
    estatus          VARCHAR(20)     NOT NULL,
    creada_por       UUID           NOT NULL,
    total_calculado  NUMERIC(10, 2)  NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_sesion         PRIMARY KEY (sesion_id),
    CONSTRAINT fk_ses_negocio    FOREIGN KEY (negocio_id)  REFERENCES negocio(negocio_id),
    CONSTRAINT fk_ses_usuario    FOREIGN KEY (creada_por)  REFERENCES usuario(usuario_id),
    CONSTRAINT ck_ses_estatus    CHECK (estatus IN ('EN_CURSO', 'FINALIZADA'))
);

-- Garantiza que solo exista una sesión EN_CURSO por negocio a nivel de base de datos
CREATE UNIQUE INDEX uq_sesion_unica_en_curso
    ON sesion (negocio_id)
    WHERE estatus = 'EN_CURSO';


-- ============================================================
-- ProductoSesion — precios ajustados por sesión, desacoplados del precio base
-- ============================================================
CREATE TABLE producto_sesion (
    producto_sesion_id UUID           NOT NULL DEFAULT gen_random_uuid(),
    sesion_id          UUID           NOT NULL,
    variante_id        UUID           NOT NULL,
    precio_sesion      NUMERIC(10, 2)  NOT NULL,
    disponible         BOOLEAN         NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_producto_sesion     PRIMARY KEY (producto_sesion_id),
    CONSTRAINT fk_ps_sesion           FOREIGN KEY (sesion_id)   REFERENCES sesion(sesion_id),
    CONSTRAINT fk_ps_variante         FOREIGN KEY (variante_id) REFERENCES variante_producto(variante_id),
    CONSTRAINT uq_ps_sesion_variante  UNIQUE (sesion_id, variante_id)
);


-- ============================================================
-- Pedido — optimistic locking con campo version
-- ============================================================
CREATE TABLE pedido (
    pedido_id         UUID           NOT NULL DEFAULT gen_random_uuid(),
    sesion_id         UUID           NOT NULL,
    direccion         VARCHAR(500),
    nombre_cliente    VARCHAR(255),
    estatus           VARCHAR(50)     NOT NULL,
    metodo_pago       VARCHAR(20)     NOT NULL,
    fecha_compromiso  DATE,
    pagado            BOOLEAN         NOT NULL DEFAULT FALSE,
    fecha_pago_real   TIMESTAMPTZ,
    metodo_solicitud  VARCHAR(20)     NOT NULL,
    tipo_envio        VARCHAR(20)     NOT NULL,
    total             NUMERIC(10, 2)  NOT NULL DEFAULT 0,
    version           BIGINT          NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_pedido         PRIMARY KEY (pedido_id),
    CONSTRAINT fk_ped_sesion     FOREIGN KEY (sesion_id) REFERENCES sesion(sesion_id),
    CONSTRAINT ck_ped_estatus    CHECK (estatus IN (
        'RECIEN_PEDIDO', 'PREPARANDO', 'EMBOLSANDO', 'ENTREGADO',
        'CANCELADO', 'DEVUELTO_ESPERANDO_CAMBIO', 'DEVUELTO_CANCELADO'
    )),
    CONSTRAINT ck_ped_metodo_pago     CHECK (metodo_pago IN ('EFECTIVO', 'TRANSFERENCIA', 'FIADO')),
    CONSTRAINT ck_ped_metodo_solicitud CHECK (metodo_solicitud IN ('WHATSAPP', 'LLAMADA', 'EN_PERSONA')),
    CONSTRAINT ck_ped_tipo_envio      CHECK (tipo_envio IN ('PROPIO', 'NORMAL', 'PASA_CLIENTE')),
    CONSTRAINT ck_ped_fiado_fecha     CHECK (
        metodo_pago != 'FIADO' OR fecha_compromiso IS NOT NULL
    )
);


-- ============================================================
-- PedidoLinea — precio_unitario congelado al crear la línea
-- ============================================================
CREATE TABLE pedido_linea (
    linea_id        UUID           NOT NULL DEFAULT gen_random_uuid(),
    pedido_id       UUID           NOT NULL,
    variante_id     UUID           NOT NULL,
    cantidad        INTEGER         NOT NULL,
    precio_unitario NUMERIC(10, 2)  NOT NULL,
    notas           VARCHAR(500),
    subtotal        NUMERIC(10, 2)  NOT NULL,
    CONSTRAINT pk_pedido_linea    PRIMARY KEY (linea_id),
    CONSTRAINT fk_pl_pedido       FOREIGN KEY (pedido_id)   REFERENCES pedido(pedido_id) ON DELETE CASCADE,
    CONSTRAINT fk_pl_variante     FOREIGN KEY (variante_id) REFERENCES variante_producto(variante_id),
    CONSTRAINT ck_pl_cantidad     CHECK (cantidad > 0)
);


-- ============================================================
-- Índices para las consultas más frecuentes
-- ============================================================
CREATE INDEX idx_negocio_usuario_usuario ON negocio_usuario (usuario_id);
CREATE INDEX idx_categoria_negocio       ON categoria (negocio_id, activa);
CREATE INDEX idx_producto_negocio        ON producto (negocio_id, activo);
CREATE INDEX idx_producto_categoria      ON producto (categoria_id);
CREATE INDEX idx_variante_producto       ON variante_producto (producto_id, activo);
CREATE INDEX idx_sesion_negocio          ON sesion (negocio_id, estatus);
CREATE INDEX idx_producto_sesion_sesion  ON producto_sesion (sesion_id, disponible);
CREATE INDEX idx_pedido_sesion           ON pedido (sesion_id, estatus);
CREATE INDEX idx_pedido_fiados           ON pedido (sesion_id, metodo_pago, pagado);
CREATE INDEX idx_pedido_linea_pedido     ON pedido_linea (pedido_id);
