package com.proyectojpa.demo.domain;

/**
 * Estados de {@link com.proyectojpa.demo.models.OrdenPago} (pago Nequi manual + conciliación).
 */
public final class OrdenPagoEstados {

    public static final String PENDIENTE = "PENDIENTE";
    /** Comprobante subido; pendiente de revisión administrativa. */
    public static final String COMPROBANTE_CARGADO = "COMPROBANTE_CARGADO";
    public static final String EN_REVISION = "EN_REVISION";
    public static final String APROBADO = "APROBADO";
    public static final String RECHAZADO = "RECHAZADO";
    public static final String VENCIDO = "VENCIDO";

    private OrdenPagoEstados() {
    }

    public static boolean esActivo(String estado) {
        return PENDIENTE.equals(estado)
                || COMPROBANTE_CARGADO.equals(estado)
                || EN_REVISION.equals(estado);
    }
}
