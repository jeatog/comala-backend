package dev.jeatog.comala.negocio.servicios;

import dev.jeatog.comala.negocio.dto.negocio.NegocioDto;

import java.util.UUID;

public interface NegocioServicio {

    NegocioDto obtener(UUID negocioId);
    NegocioDto editarNombre(UUID negocioId, String nombre);

}
