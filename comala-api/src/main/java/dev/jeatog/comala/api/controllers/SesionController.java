package dev.jeatog.comala.api.controllers;

import dev.jeatog.comala.api.models.CrearSesionReq;
import dev.jeatog.comala.api.models.ProductoSesionRes;
import dev.jeatog.comala.api.models.SesionRes;
import dev.jeatog.comala.api.seguridad.UsuarioAutenticado;
import dev.jeatog.comala.negocio.dto.sesion.CrearSesionDto;
import dev.jeatog.comala.negocio.dto.sesion.ProductoSesionEntradaDto;
import dev.jeatog.comala.negocio.dto.sesion.SesionDto;
import dev.jeatog.comala.negocio.servicios.SesionServicio;
import dev.jeatog.comala.persistencia.repositorios.UsuarioRepositorio;
import dev.jeatog.comala.websocket.models.SesionEventoMsg;
import dev.jeatog.comala.websocket.publicador.EventoPublicador;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sesion")
public class SesionController {

    private final SesionServicio sesionServicio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final EventoPublicador eventoPublicador;

    @GetMapping("/activa")
    public ResponseEntity<SesionRes> obtenerActiva(Authentication auth) {
        UsuarioAutenticado usuario = (UsuarioAutenticado) auth;
        SesionDto sesion = sesionServicio.obtenerActiva(usuario.getNegocioActivoId());
        if (sesion == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(SesionRes.de(sesion));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SesionRes> crear(
            @Valid @RequestBody CrearSesionReq req,
            Authentication auth
    ) {
        UsuarioAutenticado usuario = (UsuarioAutenticado) auth;
        UUID creadorId = usuarioRepositorio.findByEmail(usuario.getEmail())
                .orElseThrow()
                .getUsuarioId();

        List<ProductoSesionEntradaDto> productos = req.productos().stream()
                .map(p -> new ProductoSesionEntradaDto(p.varianteId(), p.precioSesion()))
                .toList();

        var dto = new CrearSesionDto(
                usuario.getNegocioActivoId(),
                creadorId,
                req.nombre(),
                productos
        );
        var resultado = sesionServicio.crear(dto);

        eventoPublicador.publicarEventoSesion(usuario.getNegocioActivoId(),
                new SesionEventoMsg(SesionEventoMsg.TipoEventoSesion.SESION_CREADA,
                        resultado.sesionId(), resultado.nombre()));

        return ResponseEntity
                .created(URI.create("/api/sesion/" + resultado.sesionId()))
                .body(SesionRes.de(resultado));
    }

    @PostMapping("/{sesionId}/finalizar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SesionRes> finalizar(
            @PathVariable UUID sesionId,
            Authentication auth
    ) {
        UsuarioAutenticado usuario = (UsuarioAutenticado) auth;
        var resultado = sesionServicio.finalizar(sesionId, usuario.getNegocioActivoId());

        eventoPublicador.publicarEventoSesion(usuario.getNegocioActivoId(),
                new SesionEventoMsg(SesionEventoMsg.TipoEventoSesion.SESION_FINALIZADA,
                        resultado.sesionId(), resultado.nombre()));

        return ResponseEntity.ok(SesionRes.de(resultado));
    }

    @GetMapping("/{sesionId}")
    public ResponseEntity<SesionRes> obtenerPorId(
            @PathVariable UUID sesionId,
            Authentication auth
    ) {
        UsuarioAutenticado usuario = (UsuarioAutenticado) auth;
        var resultado = sesionServicio.obtenerPorId(sesionId, usuario.getNegocioActivoId());
        return ResponseEntity.ok(SesionRes.de(resultado));
    }

    @GetMapping
    public ResponseEntity<List<SesionRes>> listarFinalizadas(Authentication auth) {
        UsuarioAutenticado usuario = (UsuarioAutenticado) auth;
        var resultado = sesionServicio.listarFinalizadas(usuario.getNegocioActivoId()).stream()
                .map(SesionRes::de)
                .toList();
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{sesionId}/productos")
    public ResponseEntity<List<ProductoSesionRes>> listarProductosSesion(
            @PathVariable UUID sesionId,
            Authentication auth
    ) {
        UsuarioAutenticado usuario = (UsuarioAutenticado) auth;
        var resultado = sesionServicio
                .listarProductosSesion(sesionId, usuario.getNegocioActivoId()).stream()
                .map(ProductoSesionRes::de)
                .toList();
        return ResponseEntity.ok(resultado);
    }
}
