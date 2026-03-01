package dev.jeatog.comala.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "negocio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Negocio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "negocio_id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID negocioId;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "negocio", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<NegocioUsuario> negocioUsuarios = new ArrayList<>();

    @PrePersist
    private void antesDeGuardar() {
        createdAt = Instant.now();
    }
}
