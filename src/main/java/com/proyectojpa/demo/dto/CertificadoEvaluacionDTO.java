package com.proyectojpa.demo.dto;

/**
 * Resultado de {@code CertificadoAutorizacionService#evaluarDescarga} para diagnóstico y API JSON.
 * {@code codigo} es estable para logs y automatización (no localizar en UI con este valor).
 */
public record CertificadoEvaluacionDTO(
        boolean permitido,
        String codigo,
        String mensaje,
        Integer idInscripcion,
        Integer idPersonaSesion,
        boolean certificadoFeatureHabilitado,
        Integer progresoActualPorcentaje,
        Integer progresoMinimoRequeridoPorcentaje,
        String estadoInscripcionCodigo,
        Integer estadoInscripcionId,
        boolean estadoPermiteCertificado,
        boolean usuarioEsTitularOAcudienteAutorizado) {
}
