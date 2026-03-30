package com.proyectojpa.demo.controller;

import java.time.LocalDate;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectojpa.demo.domain.InscripcionEstados;
import com.proyectojpa.demo.models.Curso;
import com.proyectojpa.demo.models.EstadoInscripcion;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.EstadoInscripcionRepository;
import com.proyectojpa.demo.repository.EstudianteRepository;
import com.proyectojpa.demo.repository.InscripcionRepository;
import com.proyectojpa.demo.repository.cursoRepository;
import com.proyectojpa.demo.security.CustomUserDetails;

@Controller
@RequestMapping("/inscripciones")
public class InscripcionController {

    @Autowired
    private cursoRepository cursoRepository;

    @Autowired
    private EstadoInscripcionRepository estadoRepo;

    @Autowired
    private InscripcionRepository inscripcionRepo;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Value("${app.inscripcion.dias-plazo-pago:7}")
    private int diasPlazoPagoInscripcion;

    /**
     * Entrada pública desde el catálogo: anónimo → login con curso; estudiante →
     * formulario de inscripción.
     */
    @GetMapping("/catalogo")
    public String desdeCatalogo(@RequestParam(name = "idCurso") Integer idCurso, Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login?curso=" + idCurso;
        }
        CustomUserDetails ud = (CustomUserDetails) auth.getPrincipal();
        if (ud.getPersona().getRolId() != null && ud.getPersona().getRolId() == 2) {
            return "redirect:/inscripciones/nueva?idCurso=" + idCurso;
        }
        return "redirect:/cursos?error=rol";
    }

    // ----------- MOSTRAR FORMULARIO -----------------
    @GetMapping({ "/nueva", "/nueva/{idEstudiante}" })
    public String nuevaInscripcion(@PathVariable(required = false) Integer idEstudiante,
            @RequestParam(name = "idCurso", required = false) Integer idCurso, // AJUSTE: Corregido nombre a idCurso y
                                                                               // añadido @RequestParam
            Model model) {

        model.addAttribute("idEstudiante", idEstudiante);
        model.addAttribute("idCurso", idCurso);

        // --- AJUSTE: Si viene un idCurso, mostramos solo ese curso en la lista para
        // evitar confusiones ---
        if (idCurso != null) {
            cursoRepository.findById(idCurso).ifPresent(curso -> {
                model.addAttribute("listaCursos", Collections.singletonList(curso));
            });
        } else {
            // Si no viene idCurso, mostramos todos (funcionalidad original corregida)
            model.addAttribute("listaCursos", cursoRepository.findAll());
        }
        // ------------------------------------------------------------------------------------------------

        model.addAttribute("listaEstados", estadoRepo.findAll());

        return "inscripcion"; // inscripcion.html en templates
    }

    // ------------ GUARDAR INSCRIPCIÓN ----------------
    @PostMapping("/guardar")
    public String guardarInscripcion(@RequestParam(name = "idCurso") Integer idCurso,
            RedirectAttributes redirectAttributes) {

        // 1. Persona logueada
        Persona personaActual = getPersona();

        if (personaActual == null) {
            return "redirect:/login";
        }

        // 2. Estudiante asociado a la persona (solo si el rol es estudiante)
        if (personaActual.getRolId() == null || personaActual.getRolId() != 2) {
            return "redirect:/cursos?error=rol";
        }
        Estudiante estudiante = estudianteRepository
                .findByPersona(personaActual)
                .orElseGet(() -> {
                    Estudiante nuevoEstudiante = new Estudiante();
                    nuevoEstudiante.setPersona(personaActual);
                    nuevoEstudiante.setProgreso("0%");
                    estadoRepo.findByCodigo(InscripcionEstados.ACTIVO).ifPresent(estado -> nuevoEstudiante.setEstadoEstudiante(estado));
                    return estudianteRepository.save(nuevoEstudiante);
                });

        // 3. Curso
        @SuppressWarnings("null")
        Curso curso = cursoRepository.findById(idCurso)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        // 4. Estado según costo del curso
        EstadoInscripcion estado;
        if (curso.getCosto() != null && curso.getCosto() > 0) {
            estado = estadoRepo.findByCodigo(InscripcionEstados.PENDIENTE_PAGO)
                    .orElseThrow(() -> new RuntimeException("Estado PENDIENTE_PAGO no configurado en BD"));
        } else {
            estado = estadoRepo.findByCodigo(InscripcionEstados.ACTIVO)
                    .orElseThrow(() -> new RuntimeException("Estado ACTIVO no configurado en BD"));
        }

        // VALIDACIÓN: evitar inscripción duplicada
        if (inscripcionRepo.existsByEstudianteAndCurso(estudiante, curso)) {
            return "redirect:/cursos?yaInscrito";
        }

        // 5. Crear inscripción
        Inscripcion insc = new Inscripcion();
        insc.setEstudiante(estudiante);
        insc.setCurso(curso);
        insc.setEstado(estado);
        insc.setFechaInscripcion(LocalDate.now());
        if (InscripcionEstados.PENDIENTE_PAGO.equals(estado.getCodigo())) {
            insc.setFechaLimitePago(LocalDate.now().plusDays(diasPlazoPagoInscripcion));
        }

        // 6. Guardar
        inscripcionRepo.save(insc);
        if (InscripcionEstados.PENDIENTE_PAGO.equals(estado.getCodigo())) {
            redirectAttributes.addFlashAttribute("exito",
                    "Inscripción registrada: pendiente de pago. Genera tu recibo desde «Mis cursos».");
        } else {
            redirectAttributes.addFlashAttribute("exito", "¡Te has inscrito correctamente en el curso!");
        }

        return "redirect:/cursos";
    }

    public Persona getPersonaActual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getPersona();
        }

        return null;
    }

    private Persona getPersona() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getPersona();
        }
        return null;
    }

}
