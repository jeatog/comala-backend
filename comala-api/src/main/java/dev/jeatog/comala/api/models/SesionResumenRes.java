package dev.jeatog.comala.api.models;

import dev.jeatog.comala.negocio.dto.sesion.SesionResumenDto;
import dev.jeatog.comala.persistencia.enums.EstatusPedido;
import dev.jeatog.comala.persistencia.enums.MetodoPago;
import dev.jeatog.comala.persistencia.enums.TipoEnvio;

import java.math.BigDecimal;
import java.util.Map;

public record SesionResumenRes(
        SesionRes sesion,
        int totalPedidos,
        Map<MetodoPago, BigDecimal> totalesPorMetodoPago,
        Map<TipoEnvio, BigDecimal> totalesPorTipoEnvio,
        Map<EstatusPedido, Integer> pedidosPorEstatus
) {
    public static SesionResumenRes de(SesionResumenDto dto) {
        return new SesionResumenRes(
                SesionRes.de(dto.sesion()),
                dto.totalPedidos(),
                dto.totalesPorMetodoPago(),
                dto.totalesPorTipoEnvio(),
                dto.pedidosPorEstatus()
        );
    }
}
