package dev.jeatog.comala.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
    name = "producto_sesion",
    uniqueConstraints = @UniqueConstraint(columnNames = {"sesion_id", "variante_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductoSesion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "producto_sesion_id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID productoSesionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sesion_id", nullable = false)
    @ToString.Exclude
    private Sesion sesion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variante_id", nullable = false)
    @ToString.Exclude
    private VarianteProducto variante;

    @Column(name = "precio_sesion", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioSesion;

    @Column(name = "disponible", nullable = false)
    private boolean disponible = true;
}
