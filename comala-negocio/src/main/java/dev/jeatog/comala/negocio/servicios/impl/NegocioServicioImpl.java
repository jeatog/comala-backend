package dev.jeatog.comala.negocio.servicios.impl;

import dev.jeatog.comala.negocio.constantes.Constantes;
import dev.jeatog.comala.negocio.dto.negocio.NegocioDto;
import dev.jeatog.comala.negocio.excepcion.ComalaExcepcion;
import dev.jeatog.comala.negocio.servicios.NegocioServicio;
import dev.jeatog.comala.persistencia.entidades.Negocio;
import dev.jeatog.comala.persistencia.repositorios.NegocioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NegocioServicioImpl implements NegocioServicio {

    private final NegocioRepositorio negocioRepositorio;

    /**
     * Obtiene la información del negocio.
     *
     * @param negocioId identificador del negocio
     * @return datos del negocio
     * @throws ComalaExcepcion 404 si no existe
     */
    @Override
    public NegocioDto obtener(UUID negocioId) {
        return toDto(buscar(negocioId));
    }

    /**
     * Edita el nombre del negocio.
     *
     * @param negocioId identificador del negocio
     * @param nombre    nuevo nombre
     * @return negocio actualizado
     * @throws ComalaExcepcion 404 si no existe
     */
    @Override
    @Transactional
    public NegocioDto editarNombre(UUID negocioId, String nombre) {
        Negocio negocio = buscar(negocioId);
        negocio.setNombre(nombre);
        return toDto(negocioRepositorio.save(negocio));
    }

    private Negocio buscar(UUID negocioId) {
        return negocioRepositorio.findById(negocioId)
                .orElseThrow(() -> new ComalaExcepcion(
                        Constantes.ERR_NEGOCIO_NO_ENCONTRADO,
                        "Negocio no encontrado.",
                        Constantes.HTTP_404_NOT_FOUND
                ));
    }

    private NegocioDto toDto(Negocio n) {
        return new NegocioDto(n.getNegocioId(), n.getNombre());
    }
}
