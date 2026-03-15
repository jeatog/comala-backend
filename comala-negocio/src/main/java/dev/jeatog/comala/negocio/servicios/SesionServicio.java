package dev.jeatog.comala.negocio.servicios;

import dev.jeatog.comala.negocio.dto.sesion.CrearSesionDto;
import dev.jeatog.comala.negocio.dto.sesion.ProductoSesionDto;
import dev.jeatog.comala.negocio.dto.sesion.SesionDto;
import dev.jeatog.comala.negocio.dto.sesion.SesionResumenDto;

import java.util.List;
import java.util.UUID;

public interface SesionServicio {

    SesionDto obtenerActiva(UUID negocioId);
    SesionDto crear(CrearSesionDto dto);
    SesionDto finalizar(UUID sesionId, UUID negocioId);
    SesionDto obtenerPorId(UUID sesionId, UUID negocioId);
    List<SesionDto> listarFinalizadas(UUID negocioId);
    List<ProductoSesionDto> listarProductosSesion(UUID sesionId, UUID negocioId);
    SesionResumenDto obtenerResumen(UUID sesionId, UUID negocioId);
    
}
