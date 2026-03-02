package dev.jeatog.comala.negocio.servicios;

import dev.jeatog.comala.negocio.dto.auth.CredencialesValidadasDto;
import dev.jeatog.comala.negocio.dto.auth.NegocioResumenDto;

import java.util.UUID;

public interface AuthServicio {

    CredencialesValidadasDto login(String email, String password);
    NegocioResumenDto validarAccesoNegocio(String email, UUID negocioId);

}
