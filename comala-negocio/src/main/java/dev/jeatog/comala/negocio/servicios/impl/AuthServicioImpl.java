package dev.jeatog.comala.negocio.servicios.impl;

import dev.jeatog.comala.negocio.constantes.Constantes;
import dev.jeatog.comala.negocio.dto.auth.CredencialesValidadasDto;
import dev.jeatog.comala.negocio.dto.auth.NegocioResumenDto;
import dev.jeatog.comala.negocio.excepcion.ComalaExcepcion;
import dev.jeatog.comala.negocio.servicios.AuthServicio;
import dev.jeatog.comala.persistencia.entidades.NegocioUsuario;
import dev.jeatog.comala.persistencia.entidades.Usuario;
import dev.jeatog.comala.persistencia.repositorios.NegocioUsuarioRepositorio;
import dev.jeatog.comala.persistencia.repositorios.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServicioImpl implements AuthServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final NegocioUsuarioRepositorio negocioUsuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    /**
     * Valida las credenciales y devuelve los datos del usuario con sus negocios activos.
     *
     * @param email    correo del usuario
     * @param password contraseña en texto plano
     * @return datos del usuario autenticado con la lista de negocios
     * @throws ComalaExcepcion 401 si las credenciales son incorrectas o el usuario está inactivo
     */
    @Override
    public CredencialesValidadasDto login(String email, String password) {
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new ComalaExcepcion(
                        Constantes.ERR_CREDENCIALES_INVALIDAS,
                        "Correo o contraseña incorrectos.",
                        Constantes.HTTP_401_UNAUTHORIZED
                ));

        if (!usuario.isActivo()) {
            throw new ComalaExcepcion(
                    Constantes.ERR_CREDENCIALES_INVALIDAS,
                    "Correo o contraseña incorrectos.",
                    Constantes.HTTP_401_UNAUTHORIZED
            );
        }

        if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
            throw new ComalaExcepcion(
                    Constantes.ERR_CREDENCIALES_INVALIDAS,
                    "Correo o contraseña incorrectos.",
                    Constantes.HTTP_401_UNAUTHORIZED
            );
        }

        List<NegocioResumenDto> negocios = negocioUsuarioRepositorio
                .findNegociosActivosPorUsuario(usuario.getUsuarioId())
                .stream()
                .map(nu -> new NegocioResumenDto(
                        nu.getNegocio().getNegocioId(),
                        nu.getNegocio().getNombre(),
                        nu.getRol()
                ))
                .toList();

        return new CredencialesValidadasDto(
                usuario.getUsuarioId(),
                usuario.getNombre(),
                usuario.getEmail(),
                negocios
        );
    }

    /**
     * Verifica que el usuario tiene acceso activo al negocio indicado.
     *
     * @param email     correo del usuario
     * @param negocioId identificador del negocio a seleccionar
     * @return resumen del negocio con el rol del usuario
     * @throws ComalaExcepcion 404 si el usuario no existe, 403 si no pertenece al negocio
     */
    @Override
    public NegocioResumenDto validarAccesoNegocio(String email, UUID negocioId) {
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new ComalaExcepcion(
                        Constantes.ERR_USUARIO_NO_ENCONTRADO,
                        "Usuario no encontrado.",
                        Constantes.HTTP_404_NOT_FOUND
                ));

        NegocioUsuario negocioUsuario = negocioUsuarioRepositorio
                .findByNegocio_NegocioIdAndUsuario_UsuarioIdAndActivoTrue(negocioId, usuario.getUsuarioId())
                .orElseThrow(() -> new ComalaExcepcion(
                        Constantes.ERR_NEGOCIO_NO_PERTENECE,
                        "No tienes acceso a este negocio.",
                        Constantes.HTTP_403_FORBIDDEN
                ));

        return new NegocioResumenDto(
                negocioUsuario.getNegocio().getNegocioId(),
                negocioUsuario.getNegocio().getNombre(),
                negocioUsuario.getRol()
        );
    }
}
