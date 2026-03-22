package com.proyectojpa.demo.domain;

/**
 * Códigos persistidos en {@code estado_inscripcion.codigo}.
 */
public final class InscripcionEstados {

    public static final String ACTIVO = "ACTIVO";
    public static final String PENDIENTE_PAGO = "PENDIENTE_PAGO";
    /** Cuenta de estudiante o inscripción anulada (plazo vencido, administración, etc.). */
    public static final String CANCELADA = "CANCELADA";
    /** Cuenta de estudiante deshabilitada manualmente (no puede iniciar sesión). */
    public static final String INACTIVO = "INACTIVO";

    private InscripcionEstados() {
    }
}
