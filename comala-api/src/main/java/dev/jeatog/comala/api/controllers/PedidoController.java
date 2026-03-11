package dev.jeatog.comala.api.controllers;

import dev.jeatog.comala.api.models.CambiarEstatusReq;
import dev.jeatog.comala.api.models.CrearPedidoReq;
import dev.jeatog.comala.api.models.EditarPedidoReq;
import dev.jeatog.comala.api.models.PedidoRes;
import dev.jeatog.comala.api.seguridad.UsuarioAutenticado;
import dev.jeatog.comala.negocio.dto.pedido.CrearPedidoDto;
import dev.jeatog.comala.negocio.dto.pedido.EditarPedidoDto;
import dev.jeatog.comala.negocio.dto.pedido.PedidoLineaEntradaDto;
import dev.jeatog.comala.negocio.servicios.PedidoServicio;
import dev.jeatog.comala.websocket.models.PedidoEventoMsg;
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
@RequestMapping("/api")
public class PedidoController {

    private final PedidoServicio pedidoServicio;
    private final EventoPublicador eventoPublicador;

    @PostMapping("/sesion/{sesionId}/pedidos")
    public ResponseEntity<PedidoRes> crear(
            @PathVariable UUID sesionId,
            @Valid @RequestBody CrearPedidoReq req,
            Authentication auth
    ) {
        UsuarioAutenticado usuario = (UsuarioAutenticado) auth;

        List<PedidoLineaEntradaDto> lineas = req.lineas().stream()
                .map(l -> new PedidoLineaEntradaDto(l.varianteId(), l.cantidad(), l.notas()))
                .toList();

        var dto = new CrearPedidoDto(
                sesionId,
                req.nombreCliente(),
                req.direccion(),
                req.metodoPago(),
                req.metodoSolicitud(),
                req.tipoEnvio(),
                req.fechaCompromiso(),
                lineas
        );

        var resultado = pedidoServicio.crear(dto, usuario.getNegocioActivoId());

        eventoPublicador.publicarEventoPedido(sesionId,
                new PedidoEventoMsg(PedidoEventoMsg.TipoEventoPedido.PEDIDO_CREADO,
                        resultado.pedidoId(), resultado));

        return ResponseEntity
                .created(URI.create("/api/pedidos/" + resultado.pedidoId()))
                .body(PedidoRes.de(resultado));
    }

    @PutMapping("/pedidos/{pedidoId}")
    public ResponseEntity<PedidoRes> editar(
            @PathVariable UUID pedidoId,
            @Valid @RequestBody EditarPedidoReq req,
            Authentication auth
    ) {
        UsuarioAutenticado usuario = (UsuarioAutenticado) auth;

        List<PedidoLineaEntradaDto> lineas = req.lineas().stream()
                .map(l -> new PedidoLineaEntradaDto(l.varianteId(), l.cantidad(), l.notas()))
                .toList();

        var dto = new EditarPedidoDto(
                pedidoId,
                usuario.getNegocioActivoId(),
                req.nombreCliente(),
                req.direccion(),
                req.metodoPago(),
                req.metodoSolicitud(),
                req.tipoEnvio(),
                req.fechaCompromiso(),
                lineas
        );

        var resultado = pedidoServicio.editar(dto);

        eventoPublicador.publicarEventoPedido(resultado.sesionId(),
                new PedidoEventoMsg(PedidoEventoMsg.TipoEventoPedido.PEDIDO_ACTUALIZADO,
                        resultado.pedidoId(), resultado));

        return ResponseEntity.ok(PedidoRes.de(resultado));
    }

    @PatchMapping("/pedidos/{pedidoId}/estatus")
    public ResponseEntity<PedidoRes> cambiarEstatus(
            @PathVariable UUID pedidoId,
            @Valid @RequestBody CambiarEstatusReq req,
            Authentication auth
    ) {
        UsuarioAutenticado usuario = (UsuarioAutenticado) auth;
        var resultado = pedidoServicio.cambiarEstatus(
                pedidoId, usuario.getNegocioActivoId(), req.estatus()
        );

        eventoPublicador.publicarEventoPedido(resultado.sesionId(),
                new PedidoEventoMsg(PedidoEventoMsg.TipoEventoPedido.ESTATUS_CAMBIADO,
                        resultado.pedidoId(), resultado));

        return ResponseEntity.ok(PedidoRes.de(resultado));
    }

    @DeleteMapping("/pedidos/{pedidoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID pedidoId,
            Authentication auth
    ) {
        UsuarioAutenticado usuario = (UsuarioAutenticado) auth;
        UUID sesionId = pedidoServicio.eliminar(pedidoId, usuario.getNegocioActivoId());

        eventoPublicador.publicarEventoPedido(sesionId,
                new PedidoEventoMsg(PedidoEventoMsg.TipoEventoPedido.PEDIDO_ELIMINADO,
                        pedidoId, null));

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pedidos/{pedidoId}")
    public ResponseEntity<PedidoRes> obtenerPorId(
            @PathVariable UUID pedidoId,
            Authentication auth
    ) {
        UsuarioAutenticado usuario = (UsuarioAutenticado) auth;
        var resultado = pedidoServicio.obtenerPorId(pedidoId, usuario.getNegocioActivoId());
        return ResponseEntity.ok(PedidoRes.de(resultado));
    }

    @GetMapping("/sesion/{sesionId}/pedidos")
    public ResponseEntity<List<PedidoRes>> listarPorSesion(
            @PathVariable UUID sesionId,
            Authentication auth
    ) {
        UsuarioAutenticado usuario = (UsuarioAutenticado) auth;
        var resultado = pedidoServicio
                .listarPorSesion(sesionId, usuario.getNegocioActivoId()).stream()
                .map(PedidoRes::de)
                .toList();
        return ResponseEntity.ok(resultado);
    }
}
