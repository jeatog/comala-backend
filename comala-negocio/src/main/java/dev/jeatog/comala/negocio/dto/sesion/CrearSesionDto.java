package dev.jeatog.comala.negocio.dto.sesion;

import java.util.List;
import java.util.UUID;

public record CrearSesionDto(
        UUID negocioId,
        UUID creadaPorId,
        String nombre,
        List<ProductoSesionEntradaDto> productos
) {}
