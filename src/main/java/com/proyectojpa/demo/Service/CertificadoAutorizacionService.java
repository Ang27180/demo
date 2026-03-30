package com.proyectojpa.demo.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.proyectojpa.demo.models.Acudiente;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.AcudienteRepository;
import com.proyectojpa.demo.repository.InscripcionRepository;

/**
 * Reglas de acceso a certificados: titular del estudiante o acudiente vinculado
 * (correo de la {@link Persona} del registro {@link Acudiente}).
 */
@Service
public class CertificadoAutorizacionService {

    private final InscripcionRepository inscripcionRepository;
    private final InscripcionAccesoService inscripcionAccesoService;
    private final ProgresoLeccionService progresoLeccionService;
    private final AcudienteRepository acudienteRepository;

    public CertificadoAutorizacionService(InscripcionRepository inscripcionRepository,
            InscripcionAccesoService inscripcionAccesoService,
            ProgresoLeccionService progresoLeccionService,
            AcudienteRepository acudienteRepository) {
        this.inscripcionRepository = inscripcionRepository;
        this.inscripcionAccesoService = inscripcionAccesoService;
        this.progresoLeccionService = progresoLeccionService;
        this.acudienteRepository = acudienteRepository;
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
        return inscripcionRepository.findByIdForCertificado(idInscripcion)
                .filter(insc -> inscripcionAccesoService.permiteCertificado(insc.getEstado()))
                .filter(insc -> progresoLeccionService.calcularPorcentaje(insc.getEstudiante(), insc.getCurso()) >= 100)
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
        if (persona.getEmail() == null || persona.getEmail().isBlank()) {
            return false;
        }
        String emailLogin = persona.getEmail().trim();
        List<Acudiente> vinculos = acudienteRepository.findByEstudianteDependienteIdEstudiante(estudiante.getIdEstudiante());
        for (Acudiente a : vinculos) {
            if (a.getPersona() != null && a.getPersona().getEmail() != null
                    && emailLogin.equalsIgnoreCase(a.getPersona().getEmail().trim())) {
                return true;
            }
        }
        return false;
    }
}
