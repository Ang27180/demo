package com.proyectojpa.demo.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.InscripcionRepository;

/**
 * Reglas de acceso a certificados: titular del estudiante o acudiente (email tutor
 * registrado en {@link Estudiante}) hasta que exista modelo formal Acudiente–Estudiante.
 */
@Service
public class CertificadoAutorizacionService {

    private final InscripcionRepository inscripcionRepository;
    private final InscripcionAccesoService inscripcionAccesoService;

    public CertificadoAutorizacionService(InscripcionRepository inscripcionRepository,
            InscripcionAccesoService inscripcionAccesoService) {
        this.inscripcionRepository = inscripcionRepository;
        this.inscripcionAccesoService = inscripcionAccesoService;
    }

    /**
     * @param idInscripcion debe existir en base de datos (validar antes con existsById si se
     *                      necesita distinguir 404 de 403).
     */
    @Transactional(readOnly = true)
    public boolean puedeDescargar(Persona persona, Integer idInscripcion) {
        if (persona == null || idInscripcion == null) {
            return false;
        }
        return inscripcionRepository.findById(idInscripcion)
                .filter(insc -> inscripcionAccesoService.permiteCertificado(insc.getEstado()))
                .map(insc -> esTitularOAcudienteProvisional(persona, insc))
                .orElse(false);
    }

    private boolean esTitularOAcudienteProvisional(Persona persona, Inscripcion inscripcion) {
        Estudiante estudiante = inscripcion.getEstudiante();
        if (estudiante == null || estudiante.getPersona() == null) {
            return false;
        }
        if (persona.getId() != null && persona.getId().equals(estudiante.getPersona().getId())) {
            return true;
        }
        String tutorEmail = estudiante.getTutorEmail();
        if (tutorEmail == null || tutorEmail.isBlank() || persona.getEmail() == null) {
            return false;
        }
        return tutorEmail.trim().equalsIgnoreCase(persona.getEmail().trim());
    }
}
