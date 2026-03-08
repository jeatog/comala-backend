package dev.jeatog.comala.api.controllers;

import dev.jeatog.comala.api.models.ProductoReq;
import dev.jeatog.comala.api.models.ProductoRes;
import dev.jeatog.comala.api.models.VarianteReq;
import dev.jeatog.comala.api.models.VarianteRes;
import dev.jeatog.comala.api.seguridad.UsuarioAutenticado;
import dev.jeatog.comala.api.servicios.AlmacenamientoServicio;
import dev.jeatog.comala.negocio.servicios.ProductoServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProductoController {

    private final ProductoServicio productoServicio;
    private final AlmacenamientoServicio almacenamientoServicio;

    // Productos
    @GetMapping("/productos")
    public ResponseEntity<List<ProductoRes>> listar(
            @RequestParam(required = false) UUID categoriaId,
            Authentication auth
    ) {
        UsuarioAutenticado principal = (UsuarioAutenticado) auth;
        List<ProductoRes> resultado = productoServicio
                .listar(principal.getNegocioActivoId(), categoriaId).stream()
                .map(ProductoRes::de)
                .toList();
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/productos/{id}")
    public ResponseEntity<ProductoRes> obtenerPorId(@PathVariable UUID id, Authentication auth) {
        UsuarioAutenticado principal = (UsuarioAutenticado) auth;
        var dto = productoServicio.obtenerPorId(id, principal.getNegocioActivoId());
        return ResponseEntity.ok(ProductoRes.de(dto));
    }

    @PostMapping("/productos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoRes> crear(
            @Valid @RequestBody ProductoReq req,
            Authentication auth
    ) {
        UsuarioAutenticado principal = (UsuarioAutenticado) auth;
        var dto = productoServicio.crear(
                principal.getNegocioActivoId(), req.categoriaId(), req.nombre());
        return ResponseEntity
                .created(URI.create("/api/productos/" + dto.productoId()))
                .body(ProductoRes.de(dto));
    }

    @PutMapping("/productos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoRes> editar(
            @PathVariable UUID id,
            @Valid @RequestBody ProductoReq req,
            Authentication auth
    ) {
        UsuarioAutenticado principal = (UsuarioAutenticado) auth;
        var dto = productoServicio.editar(
                id, principal.getNegocioActivoId(), req.nombre(), req.categoriaId());
        return ResponseEntity.ok(ProductoRes.de(dto));
    }

    @PostMapping("/productos/{id}/foto")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoRes> subirFoto(
            @PathVariable UUID id,
            @RequestParam("foto") MultipartFile foto,
            Authentication auth
    ) {
        UsuarioAutenticado principal = (UsuarioAutenticado) auth;
        String fotoUrl = almacenamientoServicio.guardar(foto);
        var dto = productoServicio.actualizarFoto(id, principal.getNegocioActivoId(), fotoUrl);
        return ResponseEntity.ok(ProductoRes.de(dto));
    }

    @PatchMapping("/productos/{id}/activo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoRes> cambiarActivo(
            @PathVariable UUID id,
            @RequestParam boolean activo,
            Authentication auth
    ) {
        UsuarioAutenticado principal = (UsuarioAutenticado) auth;
        var dto = productoServicio.cambiarActivo(id, principal.getNegocioActivoId(), activo);
        return ResponseEntity.ok(ProductoRes.de(dto));
    }

    @DeleteMapping("/productos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id, Authentication auth) {
        UsuarioAutenticado principal = (UsuarioAutenticado) auth;
        productoServicio.eliminar(id, principal.getNegocioActivoId());
        return ResponseEntity.noContent().build();
    }

    // Variantes
    @GetMapping("/productos/{productoId}/variantes")
    public ResponseEntity<List<VarianteRes>> listarVariantes(
            @PathVariable UUID productoId,
            Authentication auth
    ) {
        UsuarioAutenticado principal = (UsuarioAutenticado) auth;
        List<VarianteRes> resultado = productoServicio
                .listarVariantes(productoId, principal.getNegocioActivoId()).stream()
                .map(VarianteRes::de)
                .toList();
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/productos/{productoId}/variantes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VarianteRes> crearVariante(
            @PathVariable UUID productoId,
            @Valid @RequestBody VarianteReq req,
            Authentication auth
    ) {
        UsuarioAutenticado principal = (UsuarioAutenticado) auth;
        var dto = productoServicio.crearVariante(
                productoId, principal.getNegocioActivoId(),
                req.nombreVariante(), req.precioBase());
        return ResponseEntity
                .created(URI.create("/api/variantes/" + dto.varianteId()))
                .body(VarianteRes.de(dto));
    }

    @PutMapping("/variantes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VarianteRes> editarVariante(
            @PathVariable UUID id,
            @Valid @RequestBody VarianteReq req,
            Authentication auth
    ) {
        UsuarioAutenticado principal = (UsuarioAutenticado) auth;
        var dto = productoServicio.editarVariante(
                id, principal.getNegocioActivoId(),
                req.nombreVariante(), req.precioBase());
        return ResponseEntity.ok(VarianteRes.de(dto));
    }

    @DeleteMapping("/variantes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarVariante(@PathVariable UUID id, Authentication auth) {
        UsuarioAutenticado principal = (UsuarioAutenticado) auth;
        productoServicio.eliminarVariante(id, principal.getNegocioActivoId());
        return ResponseEntity.noContent().build();
    }
}
