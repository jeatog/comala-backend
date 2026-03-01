package dev.jeatog.comala.api.seguridad;

import dev.jeatog.comala.negocio.constantes.Constantes;
import dev.jeatog.comala.persistencia.enumeraciones.RolUsuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expiracionMs;

    public JwtUtil(
            @Value("${comala.jwt.secret}") String secret,
            @Value("${comala.jwt.expiracion-ms}") long expiracionMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    // Token JWT firmado con los campos del usuario
    public String generarToken(String email, UUID negocioActivoId, RolUsuario rol) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiracionMs);

        return Jwts.builder()
                .subject(email)
                .claim(Constantes.CLAIM_NEGOCIO_ACTIVO_ID, negocioActivoId.toString())
                .claim(Constantes.CLAIM_ROL, rol.name())
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(secretKey)
                .compact();
    }

    public boolean esTokenValido(String token) {
        try {
            extraerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extraerEmail(String token) {
        return extraerClaims(token).getSubject();
    }

    // Extrae el negocio activo del token.
    public UUID extraerNegocioActivoId(String token) {
        String id = extraerClaims(token).get(Constantes.CLAIM_NEGOCIO_ACTIVO_ID, String.class);
        return UUID.fromString(id);
    }

    // Extrae el rol del usuario del token.
    public RolUsuario extraerRol(String token) {
        String rol = extraerClaims(token).get(Constantes.CLAIM_ROL, String.class);
        return RolUsuario.valueOf(rol);
    }

    private Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
