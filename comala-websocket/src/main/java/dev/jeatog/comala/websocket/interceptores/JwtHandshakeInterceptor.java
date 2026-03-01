package dev.jeatog.comala.websocket.interceptores;

import dev.jeatog.comala.negocio.constantes.Constantes;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * Interceptor que valida el JWT durante el handshake WebSocket/STOMP.
 * El token debe enviarse como query param: /stomp?token={jwt}
 * Si el token es inválido, rechaza la conexión.
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);
    private static final String PARAM_TOKEN = "token";

    private final SecretKey secretKey;

    public JwtHandshakeInterceptor(@Value("${comala.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String query = request.getURI().getQuery();
        String token = extraerParamToken(query);

        if (token == null) {
            log.warn("Handshake WebSocket rechazado: token ausente");
            return false;
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Almacena los datos del usuario en los atributos de la sesión WebSocket
            attributes.put("email", claims.getSubject());
            attributes.put(Constantes.CLAIM_NEGOCIO_ACTIVO_ID,
                    UUID.fromString(claims.get(Constantes.CLAIM_NEGOCIO_ACTIVO_ID, String.class)));
            attributes.put(Constantes.CLAIM_ROL,
                    claims.get(Constantes.CLAIM_ROL, String.class));

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Handshake WebSocket rechazado: token inválido");
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // Sin acción posterior al handshake
    }

    private String extraerParamToken(String query) {
        if (query == null) return null;
        for (String param : query.split("&")) {
            if (param.startsWith(PARAM_TOKEN + "=")) {
                return param.substring(PARAM_TOKEN.length() + 1);
            }
        }
        return null;
    }
}
