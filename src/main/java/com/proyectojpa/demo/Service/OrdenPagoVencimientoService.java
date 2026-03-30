package com.proyectojpa.demo.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Marca órdenes Nequi vencidas (fecha de vencimiento &lt; hoy) como {@link com.proyectojpa.demo.domain.OrdenPagoEstados#VENCIDO}.
 */
@Service
public class OrdenPagoVencimientoService {

    private static final Logger log = LoggerFactory.getLogger(OrdenPagoVencimientoService.class);

    private final OrdenPagoService ordenPagoService;

    public OrdenPagoVencimientoService(OrdenPagoService ordenPagoService) {
        this.ordenPagoService = ordenPagoService;
    }

    @Scheduled(cron = "${app.pago.orden-vencimiento-cron:0 5 0 * * ?}")
    public void vencerOrdenes() {
        int n = ordenPagoService.marcarOrdenesVencidas();
        if (n > 0) {
            log.info("Órdenes de pago marcadas como VENCIDO: {}", n);
        }
    }
}
