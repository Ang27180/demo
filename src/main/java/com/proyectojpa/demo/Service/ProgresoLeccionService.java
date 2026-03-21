package com.proyectojpa.demo.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyectojpa.demo.models.Curso;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Leccion;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.models.ProgresoLeccion;
import com.proyectojpa.demo.repository.EstudianteRepository;
import com.proyectojpa.demo.repository.InscripcionRepository;
import com.proyectojpa.demo.repository.LeccionRepository;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.repository.ProgresoLeccionRepository;
import com.proyectojpa.demo.repository.cursoRepository;

@Service
public class ProgresoLeccionService {

    private final ProgresoLeccionRepository progresoLeccionRepository;
    private final LeccionRepository leccionRepository;
    private final InscripcionRepository inscripcionRepository;
    private final EstudianteRepository estudianteRepository;
    private final cursoRepository cursoRepository;
    private final InscripcionAccesoService inscripcionAccesoService;

    public ProgresoLeccionService(ProgresoLeccionRepository progresoLeccionRepository,
            LeccionRepository leccionRepository,
            InscripcionRepository inscripcionRepository,
            EstudianteRepository estudianteRepository,
            cursoRepository cursoRepository,
            InscripcionAccesoService inscripcionAccesoService) {
        this.progresoLeccionRepository = progresoLeccionRepository;
        this.leccionRepository = leccionRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.estudianteRepository = estudianteRepository;
        this.cursoRepository = cursoRepository;
        this.inscripcionAccesoService = inscripcionAccesoService;
    }

    public int calcularPorcentaje(Estudiante estudiante, Curso curso) {
        if (estudiante == null || curso == null || curso.getId() == null) {
            return 0;
        }
        java.util.Optional<Inscripcion> insc = inscripcionRepository.findByEstudianteAndCurso(estudiante, curso);
        if (insc.isEmpty() || !inscripcionAccesoService.permiteAccesoContenido(insc.get().getEstado())) {
            return 0;
        }
        long total = leccionRepository.countByModulo_Curso_Id(curso.getId());
        if (total == 0) {
            return 100;
        }
        long hechas = progresoLeccionRepository.countByEstudiante_IdEstudianteAndLeccion_Modulo_Curso_Id(
                estudiante.getIdEstudiante(), curso.getId());
        return (int) Math.round((hechas * 100.0) / total);
    }

    public List<Integer> leccionIdsCompletadas(Estudiante estudiante, Integer idCurso) {
        if (estudiante == null || idCurso == null) {
            return Collections.emptyList();
        }
        Curso curso = cursoRepository.findById(idCurso).orElse(null);
        if (curso == null) {
            return Collections.emptyList();
        }
        java.util.Optional<Inscripcion> insc = inscripcionRepository.findByEstudianteAndCurso(estudiante, curso);
        if (insc.isEmpty() || !inscripcionAccesoService.permiteAccesoContenido(insc.get().getEstado())) {
            return Collections.emptyList();
        }
        return progresoLeccionRepository.findLeccionIdsCompletadas(estudiante.getIdEstudiante(), idCurso);
    }

    /**
     * Marca la lección como completada si el estudiante está inscrito en el curso y la lección pertenece a ese curso.
     *
     * @throws IllegalArgumentException si datos inconsistentes
     * @throws IllegalStateException    si no hay inscripción activa en el curso
     */
    @Transactional
    public int marcarLeccionCompletada(Persona persona, Integer idCurso, Integer idLeccion) {
        Objects.requireNonNull(persona, "persona");
        Objects.requireNonNull(idCurso, "idCurso");
        Objects.requireNonNull(idLeccion, "idLeccion");

        Estudiante estudiante = estudianteRepository.findByPersona(persona)
                .orElseThrow(() -> new IllegalStateException("La persona no tiene perfil de estudiante"));

        Curso curso = cursoRepository.findById(idCurso)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

        Leccion leccion = leccionRepository.findById(idLeccion)
                .orElseThrow(() -> new IllegalArgumentException("Lección no encontrada"));

        if (leccion.getModulo() == null || leccion.getModulo().getCurso() == null
                || !idCurso.equals(leccion.getModulo().getCurso().getId())) {
            throw new IllegalArgumentException("La lección no pertenece al curso indicado");
        }

        java.util.Optional<Inscripcion> inscOpt = inscripcionRepository.findByEstudianteAndCurso(estudiante, curso);
        if (inscOpt.isEmpty()) {
            throw new IllegalStateException("No estás inscrito en este curso");
        }
        if (!inscripcionAccesoService.permiteAccesoContenido(inscOpt.get().getEstado())) {
            throw new IllegalStateException("Completa el pago para acceder al contenido del curso.");
        }

        if (progresoLeccionRepository.existsByEstudiante_IdEstudianteAndLeccion_Id(estudiante.getIdEstudiante(),
                idLeccion)) {
            return calcularPorcentaje(estudiante, curso);
        }

        ProgresoLeccion pl = new ProgresoLeccion();
        pl.setEstudiante(estudiante);
        pl.setLeccion(leccion);
        pl.setFechaCompletado(LocalDateTime.now());
        progresoLeccionRepository.save(pl);

        int pct = calcularPorcentaje(estudiante, curso);
        estudiante.setProgreso(pct + "%");
        estudianteRepository.save(estudiante);

        return pct;
    }
}
