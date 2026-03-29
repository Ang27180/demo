package com.proyectojpa.demo.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de {@link com.proyectojpa.demo.repository.InscripcionRepository#integridadCertificadoNativa(Integer)}:
 * qué enlaces existen vía LEFT JOIN (no exige que todas las FK estén bien).
 */
public record InscripcionCertificadoIntegridadDTO(
        Integer idInscripcion,
        boolean existeEstudiante,
        boolean existePersonaEstudiante,
        boolean existeCurso,
        boolean existeEstado) {

    public static InscripcionCertificadoIntegridadDTO fromRow(Object[] row) {
        if (row == null || row.length < 5) {
            throw new IllegalArgumentException("Fila integridad certificado inválida");
        }
        return new InscripcionCertificadoIntegridadDTO(
                row[0] != null ? ((Number) row[0]).intValue() : null,
                toBool(row[1]),
                toBool(row[2]),
                toBool(row[3]),
                toBool(row[4]));
    }

    private static boolean toBool(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Boolean b) {
            return b;
        }
        if (o instanceof Number n) {
            return n.intValue() != 0;
        }
        return false;
    }

    /** Texto para logs y {@code mensaje} en API de diagnóstico. */
    public String formatoDiagnostico() {
        List<String> rotas = new ArrayList<>();
        if (!existeEstudiante()) {
            rotas.add("inscripcion→estudiante (id_estudiante NULL o FK huérfana)");
        }
        if (!existePersonaEstudiante()) {
            rotas.add("estudiante→persona (persona_id_persona NULL o persona inexistente)");
        }
        if (!existeCurso()) {
            rotas.add("inscripcion→curso (id_curso NULL o curso inexistente)");
        }
        if (!existeEstado()) {
            rotas.add("inscripcion→estado (id_estado NULL o estado_inscripcion inexistente)");
        }
        if (rotas.isEmpty()) {
            return "LEFT JOIN indica FKs OK; si JPQL INNER FETCH sigue vacío, revisar sesión Hibernate, filtros o caché de segundo nivel.";
        }
        return String.join("; ", rotas);
    }
}
