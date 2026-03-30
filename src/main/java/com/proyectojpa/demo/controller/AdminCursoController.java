package com.proyectojpa.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectojpa.demo.Service.ProgresoLeccionService;
import com.proyectojpa.demo.Service.TutorService;
import com.proyectojpa.demo.models.Curso;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.Tutor;
import com.proyectojpa.demo.repository.InscripcionRepository;
import com.proyectojpa.demo.repository.cursoRepository;

/**
 * CRUD de cursos y vista de alumnos inscritos (administración).
 */
@Controller
@RequestMapping("/admin/cursos")
public class AdminCursoController {

    private final cursoRepository cursoRepository;
    private final TutorService tutorService;
    private final InscripcionRepository inscripcionRepository;
    private final ProgresoLeccionService progresoLeccionService;

    public AdminCursoController(cursoRepository cursoRepository, TutorService tutorService,
            InscripcionRepository inscripcionRepository, ProgresoLeccionService progresoLeccionService) {
        this.cursoRepository = cursoRepository;
        this.tutorService = tutorService;
        this.inscripcionRepository = inscripcionRepository;
        this.progresoLeccionService = progresoLeccionService;
    }

    @GetMapping("/form")
    public String nuevo(Model model) {
        model.addAttribute("curso", new Curso());
        model.addAttribute("personasTutor", tutorService.listarPersonasRolTutor());
        return "admin-curso-form";
    }

    @GetMapping("/form/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        Curso curso = cursoRepository.findByIdWithTutor(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + id));
        model.addAttribute("curso", curso);
        model.addAttribute("personasTutor", tutorService.listarPersonasRolTutor());
        return "admin-curso-form";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam(required = false) Integer id,
            @RequestParam String duracion,
            @RequestParam String numcurso,
            @RequestParam String nombre,
            @RequestParam(required = false) String detalle,
            @RequestParam(required = false) Double costo,
            @RequestParam(required = false) String aprendizaje,
            @RequestParam(required = false) Integer categoria,
            @RequestParam(required = false) String imagen,
            @RequestParam(name = "idPersonaTutor", required = false) Integer idPersonaTutor,
            RedirectAttributes redirectAttributes) {
        Curso c = id != null
                ? cursoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"))
                : new Curso();
        c.setDuracion(duracion);
        c.setNumcurso(numcurso);
        c.setNombre(nombre);
        c.setDetalle(detalle);
        c.setCosto(costo);
        c.setAprendizaje(aprendizaje);
        c.setCategoria(categoria);
        c.setImagen(imagen);
        if (idPersonaTutor != null) {
            Tutor t = tutorService.obtenerOCrearTutorParaPersonaId(idPersonaTutor);
            c.setTutor(t);
        } else {
            c.setTutor(null);
        }
        cursoRepository.save(c);
        redirectAttributes.addFlashAttribute("msgAdmin", "Curso guardado correctamente.");
        return "redirect:/admin";
    }

    /**
     * Asignación rápida de tutor desde la tabla del panel admin.
     * Permite asignar por idTutor (o dejar sin asignar).
     */
    @PostMapping("/{id}/asignar-tutor")
    public String asignarTutor(@PathVariable("id") Integer id,
            @RequestParam(name = "idPersonaTutor", required = false) Integer idPersonaTutor,
            RedirectAttributes redirectAttributes) {
        Curso c = cursoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + id));

        if (idPersonaTutor != null) {
            Tutor t = tutorService.obtenerOCrearTutorParaPersonaId(idPersonaTutor);
            c.setTutor(t);
        } else {
            c.setTutor(null);
        }

        cursoRepository.save(c);
        redirectAttributes.addFlashAttribute("msgAdmin", "Tutor asignado correctamente.");
        return "redirect:/admin";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            cursoRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("msgAdmin", "Curso eliminado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorAdmin", "No se pudo eliminar el curso: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @GetMapping("/{idCurso}/alumnos")
    public String alumnos(@PathVariable Integer idCurso, Model model) {
        Curso curso = cursoRepository.findByIdWithTutor(idCurso)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));
        List<Inscripcion> inscripciones = inscripcionRepository.findByCursoIdWithEstudianteAndEstado(idCurso);
        Map<Integer, Integer> progresoPorInscripcion = new HashMap<>();
        for (Inscripcion i : inscripciones) {
            int pct = progresoLeccionService.calcularPorcentaje(i.getEstudiante(), i.getCurso());
            progresoPorInscripcion.put(i.getId(), pct);
        }
        model.addAttribute("curso", curso);
        model.addAttribute("inscripciones", inscripciones);
        model.addAttribute("progresoPorInscripcion", progresoPorInscripcion);
        model.addAttribute("esVistaAdmin", true);
        return "admin-curso-alumnos";
    }
}
