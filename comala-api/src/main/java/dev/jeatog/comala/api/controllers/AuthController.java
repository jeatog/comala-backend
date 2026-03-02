package dev.jeatog.comala.api.controllers;

import dev.jeatog.comala.api.models.LoginReq;
import dev.jeatog.comala.api.models.LoginRes;
import dev.jeatog.comala.api.models.SeleccionarNegocioReq;
import dev.jeatog.comala.api.seguridad.JwtUtil;
import dev.jeatog.comala.negocio.constantes.Constantes;
import dev.jeatog.comala.negocio.dto.auth.CredencialesValidadasDto;
import dev.jeatog.comala.negocio.dto.auth.NegocioResumenDto;
import dev.jeatog.comala.negocio.excepcion.ComalaExcepcion;
import dev.jeatog.comala.negocio.servicios.AuthServicio;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthServicio authServicio;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginRes> login(@Valid @RequestBody LoginReq req) {
        CredencialesValidadasDto credenciales = authServicio.login(req.email(), req.password());
        List<NegocioResumenDto> negocios = credenciales.negocios();

        if (negocios.isEmpty()) {
            throw new ComalaExcepcion(
                    Constantes.ERR_ACCESO_DENEGADO,
                    "No tienes negocios activos asignados.",
                    Constantes.HTTP_403_FORBIDDEN
            );
        }

        if (negocios.size() == 1) {
            NegocioResumenDto negocio = negocios.getFirst();
            String token = jwtUtil.generarToken(credenciales.email(), negocio.negocioId(), negocio.rol());
            return ResponseEntity.ok(new LoginRes(token, false, null));
        }

        // Usuario con múltiples negocios: emitir token de selección de vida corta
        String tokenSeleccion = jwtUtil.generarTokenSeleccion(credenciales.email());
        return ResponseEntity.ok(new LoginRes(tokenSeleccion, true, negocios));
    }

    @PostMapping("/seleccionar-negocio")
    public ResponseEntity<LoginRes> seleccionarNegocio(@Valid @RequestBody SeleccionarNegocioReq req) {
        String email = jwtUtil.extraerEmailDeTokenSeleccion(req.tokenSeleccion());
        NegocioResumenDto negocio = authServicio.validarAccesoNegocio(email, req.negocioId());
        String token = jwtUtil.generarToken(email, negocio.negocioId(), negocio.rol());
        return ResponseEntity.ok(new LoginRes(token, false, null));
    }
}
