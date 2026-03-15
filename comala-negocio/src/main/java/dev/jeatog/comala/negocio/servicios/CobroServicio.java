package dev.jeatog.comala.negocio.servicios;

import dev.jeatog.comala.negocio.dto.cobro.CobrarDto;
import dev.jeatog.comala.negocio.dto.cobro.CobroDto;

import java.util.List;
import java.util.UUID;

public interface CobroServicio {

    List<CobroDto> listarFiados(UUID negocioId, String filtro);
    CobroDto cobrar(CobrarDto dto);
    
}
