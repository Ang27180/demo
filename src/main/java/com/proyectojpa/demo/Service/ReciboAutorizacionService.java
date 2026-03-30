package com.proyectojpa.demo.Service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyectojpa.demo.models.Acudiente;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.models.Recibo;
import com.proyectojpa.demo.repository.AcudienteRepository;

@Service
public class ReciboAutorizacionService {

    private final AcudienteRepository acudienteRepository;

    public ReciboAutorizacionService(AcudienteRepository acudienteRepository) {
        this.acudienteRepository = acudienteRepository;
    }

    /**
     * Puede ver/descargar el recibo: administrador, estudiante titular o acudiente vinculado al estudiante.
     */
    @Transactional(readOnly = true)
    public boolean puedeVerRecibo(Persona persona, Recibo recibo) {
        if (persona == null || recibo == null || recibo.getInscripcion() == null
                || recibo.getInscripcion().getEstudiante() == null
                || recibo.getInscripcion().getEstudiante().getPersona() == null) {
            return false;
        }
        if (persona.getRolId() != null && persona.getRolId() == 1) {
            return true;
        }
        Estudiante est = recibo.getInscripcion().getEstudiante();
        if (persona.getId() != null && persona.getId().equals(est.getPersona().getId())) {
            return true;
        }
        return esAcudienteDeEstudiante(persona, est);
    }

    /**
     * La persona es acudiente registrado para el estudiante (tabla {@code acudiente}).
     */
    @Transactional(readOnly = true)
    public boolean esAcudienteDeEstudiante(Persona persona, Estudiante estudiante) {
        if (persona == null || persona.getId() == null || estudiante == null || estudiante.getIdEstudiante() == null) {
            return false;
        }
        List<Acudiente> vinculos = acudienteRepository
                .findByEstudianteDependienteIdEstudianteWithDetalle(estudiante.getIdEstudiante());
        for (Acudiente a : vinculos) {
            if (a.getPersona() != null && persona.getId().equals(a.getPersona().getId())) {
                return true;
            }
        }
        return false;
    }
}
