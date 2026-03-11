package dev.jeatog.comala.api.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CrearSesionReq(
        @NotBlank String nombre,
        @NotEmpty @Valid List<ProductoSesionEntradaReq> productos
) {}
