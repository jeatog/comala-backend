package dev.jeatog.comala.api.controllers;

import dev.jeatog.comala.api.models.RegistrarUsuarioReq;
import dev.jeatog.comala.api.models.UsuarioRes;
import dev.jeatog.comala.api.seguridad.PrincipalComala;
import dev.jeatog.comala.api.servicios.AlmacenamientoServicio;
import dev.jeatog.comala.negocio.dto.usuario.RegistrarUsuarioDto;
import dev.jeatog.comala.negocio.servicios.UsuarioServicio;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioServicio usuarioServicio;
    private final AlmacenamientoServicio almacenamientoServicio;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioRes> registrar(
            @Valid @RequestBody RegistrarUsuarioReq req,
            Authentication auth
    ) {
        PrincipalComala principal = (PrincipalComala) auth;
        var dto = new RegistrarUsuarioDto(
                principal.getNegocioActivoId(),
                req.nombre(),
                req.email(),
                req.password(),
                req.rol()
        );
        var resultado = usuarioServicio.registrar(dto);
        return ResponseEntity
                .created(URI.create("/api/usuarios/" + resultado.usuarioId()))
                .body(UsuarioRes.de(resultado));
    }

    @PostMapping("/{usuarioId}/foto")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioRes> subirFoto(
            @PathVariable UUID usuarioId,
            @RequestParam("foto") MultipartFile foto,
            Authentication auth
    ) {
        PrincipalComala principal = (PrincipalComala) auth;
        String fotoUrl = almacenamientoServicio.guardar(foto);
        var resultado = usuarioServicio.actualizarFoto(usuarioId, principal.getNegocioActivoId(), fotoUrl);
        return ResponseEntity.ok(UsuarioRes.de(resultado));
    }
}
