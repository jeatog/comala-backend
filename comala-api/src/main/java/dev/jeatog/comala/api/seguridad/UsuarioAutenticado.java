package dev.jeatog.comala.api.seguridad;

import dev.jeatog.comala.persistencia.enums.RolUsuario;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

/**
 * Representa al usuario autenticado dentro del SecurityContext.
 * Contiene el email, el negocio activo y el rol extraídos del JWT.
 */
public class UsuarioAutenticado extends AbstractAuthenticationToken {

    private final String email;
    private final UUID negocioActivoId;
    private final RolUsuario rol;

    public UsuarioAutenticado(String email, UUID negocioActivoId, RolUsuario rol) {
        super(List.of(new SimpleGrantedAuthority("ROLE_" + rol.name())));
        this.email = email;
        this.negocioActivoId = negocioActivoId;
        this.rol = rol;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return email;
    }

    public String getEmail() {
        return email;
    }

    public UUID getNegocioActivoId() {
        return negocioActivoId;
    }

    public RolUsuario getRol() {
        return rol;
    }
}
