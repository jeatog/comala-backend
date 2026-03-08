package dev.jeatog.comala.persistencia.entidades;

import dev.jeatog.comala.persistencia.enums.EstatusSesion;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sesion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "sesion_id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID sesionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "negocio_id", nullable = false)
    @ToString.Exclude
    private Negocio negocio;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus", nullable = false, length = 20)
    private EstatusSesion estatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creada_por", nullable = false)
    @ToString.Exclude
    private Usuario creadaPor;

    @Column(name = "total_calculado", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCalculado = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "sesion", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ProductoSesion> productosSesion = new ArrayList<>();

    @OneToMany(mappedBy = "sesion", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Pedido> pedidos = new ArrayList<>();

    @PrePersist
    private void antesDeGuardar() {
        createdAt = Instant.now();
    }
}
