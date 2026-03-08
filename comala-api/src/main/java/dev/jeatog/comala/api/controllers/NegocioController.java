package dev.jeatog.comala.api.controllers;

import dev.jeatog.comala.api.models.NegocioReq;
import dev.jeatog.comala.api.models.NegocioRes;
import dev.jeatog.comala.api.seguridad.PrincipalComala;
import dev.jeatog.comala.negocio.servicios.NegocioServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/negocio")
public class NegocioController {

    private final NegocioServicio negocioServicio;

    @GetMapping
    public ResponseEntity<NegocioRes> obtener(Authentication auth) {
        PrincipalComala principal = (PrincipalComala) auth;
        var dto = negocioServicio.obtener(principal.getNegocioActivoId());
        return ResponseEntity.ok(NegocioRes.de(dto));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NegocioRes> editarNombre(
            @Valid @RequestBody NegocioReq req,
            Authentication auth
    ) {
        PrincipalComala principal = (PrincipalComala) auth;
        var dto = negocioServicio.editarNombre(principal.getNegocioActivoId(), req.nombre());
        return ResponseEntity.ok(NegocioRes.de(dto));
    }
}
