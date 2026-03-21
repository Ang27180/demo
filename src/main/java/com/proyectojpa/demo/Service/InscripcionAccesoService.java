package com.proyectojpa.demo.Service;

import org.springframework.stereotype.Service;

import com.proyectojpa.demo.domain.InscripcionEstados;
import com.proyectojpa.demo.models.EstadoInscripcion;

@Service
public class InscripcionAccesoService {

    public boolean permiteAccesoContenido(EstadoInscripcion estado) {
        if (estado == null) {
            return false;
        }
        if (InscripcionEstados.ACTIVO.equals(estado.getCodigo())) {
            return true;
        }
        // Compatibilidad: estados antiguos sin código (p. ej. id=1 histórico)
        return estado.getCodigo() == null && estado.getId() != null && estado.getId() == 1;
    }

    public boolean permiteCertificado(EstadoInscripcion estado) {
        return permiteAccesoContenido(estado);
    }
}
