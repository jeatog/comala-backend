package dev.jeatog.comala.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "categoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "categoria_id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID categoriaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "negocio_id", nullable = false)
    @ToString.Exclude
    private Negocio negocio;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "activa", nullable = false)
    private boolean activa = true;

    @Column(name = "orden", nullable = false)
    private int orden = 0;

    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Producto> productos = new ArrayList<>();
}
