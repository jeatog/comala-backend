package dev.jeatog.comala.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "variante_producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VarianteProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "variante_id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID varianteId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    @ToString.Exclude
    private Producto producto;

    @Column(name = "nombre_variante", nullable = false, length = 255)
    private String nombreVariante;

    @Column(name = "precio_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;
}
