package dev.jeatog.comala.persistencia.entidades;

import dev.jeatog.comala.persistencia.enums.RolUsuario;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
    name = "negocio_usuario",
    uniqueConstraints = @UniqueConstraint(columnNames = {"negocio_id", "usuario_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NegocioUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "negocio_usuario_id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID negocioUsuarioId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "negocio_id", nullable = false)
    @ToString.Exclude
    private Negocio negocio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private RolUsuario rol;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;
}
