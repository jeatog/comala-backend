package dev.jeatog.comala.api.seguridad;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que intercepta cada request, extrae el JWT del header Authorization
 * y, si es válido, establece el principal autenticado en el SecurityContext.
 */
@Component
@RequiredArgsConstructor
public class JwtFiltro extends OncePerRequestFilter {

    private static final String HEADER_AUTH  = "Authorization";
    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HEADER_AUTH);

        if (authHeader == null || !authHeader.startsWith(PREFIJO_BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(PREFIJO_BEARER.length());

        if (!jwtUtil.esTokenValido(token) || jwtUtil.esTokenDeSeleccion(token)) {
            // Tokens inválidos o de selección no autentican requests normales
            filterChain.doFilter(request, response);
            return;
        }

        UsuarioAutenticado principal = new UsuarioAutenticado(
                jwtUtil.extraerEmail(token),
                jwtUtil.extraerNegocioActivoId(token),
                jwtUtil.extraerRol(token)
        );

        SecurityContextHolder.getContext().setAuthentication(principal);
        filterChain.doFilter(request, response);
    }
}
