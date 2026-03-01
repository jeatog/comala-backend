package dev.jeatog.comala.persistencia.entidades;

import dev.jeatog.comala.persistencia.enumeraciones.EstatusPedido;
import dev.jeatog.comala.persistencia.enumeraciones.MetodoPago;
import dev.jeatog.comala.persistencia.enumeraciones.MetodoSolicitud;
import dev.jeatog.comala.persistencia.enumeraciones.TipoEnvio;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "pedido_id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID pedidoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sesion_id", nullable = false)
    @ToString.Exclude
    private Sesion sesion;

    @Column(name = "direccion", length = 500)
    private String direccion;

    @Column(name = "nombre_cliente", length = 255)
    private String nombreCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus", nullable = false, length = 50)
    private EstatusPedido estatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;

    @Column(name = "fecha_compromiso")
    private LocalDate fechaCompromiso;

    @Column(name = "pagado", nullable = false)
    private boolean pagado = false;

    @Column(name = "fecha_pago_real")
    private Instant fechaPagoReal;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_solicitud", nullable = false, length = 20)
    private MetodoSolicitud metodoSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_envio", nullable = false, length = 20)
    private TipoEnvio tipoEnvio;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PedidoLinea> lineas = new ArrayList<>();

    @PrePersist
    private void antesDeGuardar() {
        createdAt = Instant.now();
    }
}
