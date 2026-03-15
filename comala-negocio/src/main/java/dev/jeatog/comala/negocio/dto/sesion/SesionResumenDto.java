package dev.jeatog.comala.negocio.dto.sesion;

import dev.jeatog.comala.persistencia.enums.EstatusPedido;
import dev.jeatog.comala.persistencia.enums.MetodoPago;
import dev.jeatog.comala.persistencia.enums.TipoEnvio;

import java.math.BigDecimal;
import java.util.Map;

public record SesionResumenDto(
        SesionDto sesion,
        int totalPedidos,
        Map<MetodoPago, BigDecimal> totalesPorMetodoPago,
        Map<TipoEnvio, BigDecimal> totalesPorTipoEnvio,
        Map<EstatusPedido, Integer> pedidosPorEstatus
) {}
