package dev.jeatog.comala.api.controllers;

import dev.jeatog.comala.api.models.CobrarReq;
import dev.jeatog.comala.api.models.CobroRes;
import dev.jeatog.comala.api.seguridad.UsuarioAutenticado;
import dev.jeatog.comala.negocio.dto.cobro.CobrarDto;
import dev.jeatog.comala.negocio.servicios.CobroServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cobros")
public class CobroController {

    private final CobroServicio cobroServicio;

    @GetMapping
    public ResponseEntity<List<CobroRes>> listar(
            @RequestParam(required = false) String filtro,
            Authentication auth
    ) {
        UsuarioAutenticado usuario = (UsuarioAutenticado) auth;
        var resultado = cobroServicio
                .listarFiados(usuario.getNegocioActivoId(), filtro).stream()
                .map(CobroRes::de)
                .toList();
        return ResponseEntity.ok(resultado);
    }

    @PatchMapping("/{pedidoId}/cobrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<CobroRes> cobrar(
            @PathVariable UUID pedidoId,
            @Valid @RequestBody CobrarReq req,
            Authentication auth
    ) {
        UsuarioAutenticado usuario = (UsuarioAutenticado) auth;
        var dto = new CobrarDto(pedidoId, usuario.getNegocioActivoId(), req.metodoPagoReal());
        var resultado = cobroServicio.cobrar(dto);
        return ResponseEntity.ok(CobroRes.de(resultado));
    }
}
