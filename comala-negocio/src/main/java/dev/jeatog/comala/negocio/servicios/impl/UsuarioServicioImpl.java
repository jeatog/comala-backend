package dev.jeatog.comala.negocio.servicios.impl;

import dev.jeatog.comala.negocio.constantes.Constantes;
import dev.jeatog.comala.negocio.dto.usuario.RegistrarUsuarioDto;
import dev.jeatog.comala.negocio.dto.usuario.UsuarioDto;
import dev.jeatog.comala.negocio.excepcion.ComalaExcepcion;
import dev.jeatog.comala.negocio.servicios.UsuarioServicio;
import dev.jeatog.comala.persistencia.entidades.Negocio;
import dev.jeatog.comala.persistencia.entidades.NegocioUsuario;
import dev.jeatog.comala.persistencia.entidades.Usuario;
import dev.jeatog.comala.persistencia.enums.RolUsuario;
import dev.jeatog.comala.persistencia.repositorios.NegocioRepositorio;
import dev.jeatog.comala.persistencia.repositorios.NegocioUsuarioRepositorio;
import dev.jeatog.comala.persistencia.repositorios.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioServicioImpl implements UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final NegocioRepositorio negocioRepositorio;
    private final NegocioUsuarioRepositorio negocioUsuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crea un nuevo usuario y lo asocia al negocio con el rol indicado.
     *
     * @param dto datos del usuario a registrar (negocioId, nombre, email, password, rol)
     * @return datos del usuario creado
     * @throws ComalaExcepcion 409 si el correo ya está en uso, 404 si el negocio no existe
     */
    @Override
    @Transactional
    public UsuarioDto registrar(RegistrarUsuarioDto dto) {
        if (usuarioRepositorio.existsByEmail(dto.email())) {
            throw new ComalaExcepcion(
                    Constantes.ERR_USUARIO_CONFLICTO,
                    "Ya existe un usuario registrado con ese correo.",
                    Constantes.HTTP_409_CONFLICT
            );
        }

        Negocio negocio = negocioRepositorio.findById(dto.negocioId())
                .orElseThrow(() -> new ComalaExcepcion(
                        Constantes.ERR_NEGOCIO_NO_ENCONTRADO,
                        "Negocio no encontrado.",
                        Constantes.HTTP_404_NOT_FOUND
                ));

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.nombre());
        usuario.setEmail(dto.email());
        usuario.setPasswordHash(passwordEncoder.encode(dto.password()));
        usuario.setActivo(true);
        usuario = usuarioRepositorio.save(usuario);

        NegocioUsuario negocioUsuario = new NegocioUsuario();
        negocioUsuario.setNegocio(negocio);
        negocioUsuario.setUsuario(usuario);
        negocioUsuario.setRol(dto.rol());
        negocioUsuario.setActivo(true);
        negocioUsuarioRepositorio.save(negocioUsuario);

        return toDto(usuario, dto.rol());
    }

    /**
     * Actualiza la URL de foto de perfil del usuario, validando que pertenezca al negocio del token.
     *
     * @param usuarioId identificador del usuario a modificar
     * @param negocioId negocio activo del token (para validar pertenencia)
     * @param fotoUrl   URL pública de la nueva foto
     * @return datos actualizados del usuario
     * @throws ComalaExcepcion 404 si el usuario no existe, 403 si no pertenece al negocio
     */
    @Override
    @Transactional
    public UsuarioDto actualizarFoto(UUID usuarioId, UUID negocioId, String fotoUrl) {
        Usuario usuario = usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new ComalaExcepcion(
                        Constantes.ERR_USUARIO_NO_ENCONTRADO,
                        "Usuario no encontrado.",
                        Constantes.HTTP_404_NOT_FOUND
                ));

        // Verifica que el usuario pertenece al negocio del token
        negocioUsuarioRepositorio
                .findByNegocio_NegocioIdAndUsuario_UsuarioIdAndActivoTrue(negocioId, usuarioId)
                .orElseThrow(() -> new ComalaExcepcion(
                        Constantes.ERR_ACCESO_DENEGADO,
                        "No puedes modificar un usuario de otro negocio.",
                        Constantes.HTTP_403_FORBIDDEN
                ));

        usuario.setFotoUrl(fotoUrl);
        usuario = usuarioRepositorio.save(usuario);

        var rolUsuario = negocioUsuarioRepositorio
                .findByNegocio_NegocioIdAndUsuario_UsuarioIdAndActivoTrue(negocioId, usuarioId)
                .map(NegocioUsuario::getRol)
                .orElseThrow();

        return toDto(usuario, rolUsuario);
    }

    /**
     * Cambia el nombre del usuario autenticado.
     *
     * @param email       correo del usuario (del JWT)
     * @param negocioId   negocio activo (para obtener rol en el DTO)
     * @param nuevoNombre nuevo nombre
     * @return datos actualizados del usuario
     * @throws ComalaExcepcion 404 si el usuario no existe
     */
    @Override
    @Transactional
    public UsuarioDto cambiarNombre(String email, UUID negocioId, String nuevoNombre) {
        Usuario usuario = buscarPorEmail(email);
        usuario.setNombre(nuevoNombre);
        usuario = usuarioRepositorio.save(usuario);
        return toDto(usuario, obtenerRol(negocioId, usuario.getUsuarioId()));
    }

    /**
     * Cambia la contraseña del usuario autenticado, validando la contraseña actual.
     *
     * @param email          correo del usuario (del JWT)
     * @param passwordActual contraseña actual en texto plano
     * @param nuevaPassword  nueva contraseña en texto plano
     * @throws ComalaExcepcion 404 si el usuario no existe, 401 si la contraseña actual es incorrecta
     */
    @Override
    @Transactional
    public void cambiarPassword(String email, String passwordActual, String nuevaPassword) {
        Usuario usuario = buscarPorEmail(email);

        if (!passwordEncoder.matches(passwordActual, usuario.getPasswordHash())) {
            throw new ComalaExcepcion(
                    Constantes.ERR_CREDENCIALES_INVALIDAS,
                    "La contraseña actual es incorrecta.",
                    Constantes.HTTP_401_UNAUTHORIZED
            );
        }

        usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        usuarioRepositorio.save(usuario);
    }

    /**
     * Cambia la foto de perfil del usuario autenticado.
     *
     * @param email     correo del usuario (del JWT)
     * @param negocioId negocio activo (para obtener rol en el DTO)
     * @param fotoUrl   URL de la nueva foto
     * @return datos actualizados del usuario
     * @throws ComalaExcepcion 404 si el usuario no existe
     */
    @Override
    @Transactional
    public UsuarioDto cambiarFotoPropia(String email, UUID negocioId, String fotoUrl) {
        Usuario usuario = buscarPorEmail(email);
        usuario.setFotoUrl(fotoUrl);
        usuario = usuarioRepositorio.save(usuario);
        return toDto(usuario, obtenerRol(negocioId, usuario.getUsuarioId()));
    }

    private Usuario buscarPorEmail(String email) {
        return usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new ComalaExcepcion(
                        Constantes.ERR_USUARIO_NO_ENCONTRADO,
                        "Usuario no encontrado.",
                        Constantes.HTTP_404_NOT_FOUND
                ));
    }

    private RolUsuario obtenerRol(UUID negocioId, UUID usuarioId) {
        return negocioUsuarioRepositorio
                .findByNegocio_NegocioIdAndUsuario_UsuarioIdAndActivoTrue(negocioId, usuarioId)
                .map(NegocioUsuario::getRol)
                .orElseThrow();
    }

    private UsuarioDto toDto(Usuario usuario, RolUsuario rol) {
        return new UsuarioDto(
                usuario.getUsuarioId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getFotoUrl(),
                rol
        );
    }
}
