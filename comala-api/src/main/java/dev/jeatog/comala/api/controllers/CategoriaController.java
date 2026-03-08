package dev.jeatog.comala.api.controllers;

import dev.jeatog.comala.api.models.CategoriaReq;
import dev.jeatog.comala.api.models.CategoriaRes;
import dev.jeatog.comala.api.seguridad.PrincipalComala;
import dev.jeatog.comala.negocio.servicios.CategoriaServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaServicio categoriaServicio;

    @GetMapping
    public ResponseEntity<List<CategoriaRes>> listar(Authentication auth) {
        PrincipalComala principal = (PrincipalComala) auth;
        List<CategoriaRes> resultado = categoriaServicio
                .listar(principal.getNegocioActivoId()).stream()
                .map(CategoriaRes::de)
                .toList();
        return ResponseEntity.ok(resultado);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaRes> crear(
            @Valid @RequestBody CategoriaReq req,
            Authentication auth
    ) {
        PrincipalComala principal = (PrincipalComala) auth;
        var dto = categoriaServicio.crear(principal.getNegocioActivoId(), req.nombre());
        return ResponseEntity
                .created(URI.create("/api/categorias/" + dto.categoriaId()))
                .body(CategoriaRes.de(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaRes> editar(
            @PathVariable UUID id,
            @Valid @RequestBody CategoriaReq req,
            Authentication auth
    ) {
        PrincipalComala principal = (PrincipalComala) auth;
        var dto = categoriaServicio.editar(id, principal.getNegocioActivoId(), req.nombre());
        return ResponseEntity.ok(CategoriaRes.de(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id, Authentication auth) {
        PrincipalComala principal = (PrincipalComala) auth;
        categoriaServicio.eliminar(id, principal.getNegocioActivoId());
        return ResponseEntity.noContent().build();
    }
}
