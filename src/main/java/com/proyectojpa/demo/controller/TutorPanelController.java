package com.proyectojpa.demo.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.proyectojpa.demo.Service.ProgresoLeccionService;
import com.proyectojpa.demo.models.Curso;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.models.Tutor;
import com.proyectojpa.demo.repository.InscripcionRepository;
import com.proyectojpa.demo.repository.PersonaRepository;
import com.proyectojpa.demo.repository.TutorRepository;
import com.proyectojpa.demo.repository.cursoRepository;
import com.proyectojpa.demo.security.CustomUserDetails;

@Controller
public class TutorPanelController {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final int ROL_TUTOR = 3;
    private static final int TUTOR_PAGE_SIZE = 10;

    @Autowired
    private TutorRepository tutorRepo;

    @Autowired
    private PersonaRepository personaRepo;

    @Autowired
    private InscripcionRepository inscripcionRepo;

    @Autowired
    private cursoRepository cursoRepo;

    @Autowired
    private ProgresoLeccionService progresoLeccionService;

    @GetMapping("/tutor-panel")
    public String dashboard(
            Model model,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "filtroIdPersona", required = false) Integer filtroIdPersona,
            @RequestParam(name = "filtroIdTutor", required = false) Integer filtroIdTutor,
            @RequestParam(name = "filtroIdCurso", required = false) Integer filtroIdCurso) {
        Persona persona = getPersonaActual();
        if (persona == null) {
            return "redirect:/login";
        }
        model.addAttribute("persona", persona);

        if (esAdministrador()) {
            Integer filtroPersona = resolverFiltroIdPersona(filtroIdPersona, filtroIdTutor);
            Page<Persona> personasTutorPage = paginaPersonasTutorAdmin(page, filtroPersona, filtroIdCurso);
            List<Curso> todosCursos = cursoRepo.findAllWithTutorPersonaOrderById();

            Map<Integer, List<Curso>> cursosAsignadosPorPersonaId = new LinkedHashMap<>();
            Map<Integer, List<Curso>> cursosMostrarPorPersonaId = new LinkedHashMap<>();
            for (Persona p : personasTutorPage.getContent()) {
                List<Curso> asignados = tutorRepo.findByPersona(p)
                        .map(t -> cursoRepo.findByTutor_IdTutorWithTutorPersona(t.getIdTutor()))
                        .orElse(Collections.emptyList());
                cursosAsignadosPorPersonaId.put(p.getId(), asignados);
                cursosMostrarPorPersonaId.put(p.getId(), filtrarCursosLista(asignados, filtroIdCurso));
            }

            model.addAttribute("vistaSupervisionAdmin", true);
            model.addAttribute("personasTutorPage", personasTutorPage);
            model.addAttribute("cursosAsignadosPorPersonaId", cursosAsignadosPorPersonaId);
            model.addAttribute("cursosMostrarPorPersonaId", cursosMostrarPorPersonaId);
            model.addAttribute("hayPersonasRolTutor", personaRepo.countByRolId(ROL_TUTOR) > 0);
            model.addAttribute("cursosTutor", new ArrayList<Curso>());
            model.addAttribute("filtroIdPersona", filtroPersona);
            model.addAttribute("filtroIdCurso", filtroIdCurso);
            model.addAttribute("personasTutorFiltroSelect", personaRepo.findByRolIdOrderByNombreAsc(ROL_TUTOR));
            model.addAttribute("cursosFiltroSelect", todosCursos);
            return "tutor";
        }

        Tutor tutor = tutorRepo.findByPersona(persona).orElse(null);
        List<Curso> cursos = (tutor != null)
                ? cursoRepo.findByTutor_IdTutorWithTutorPersona(tutor.getIdTutor())
                : new ArrayList<>();
        model.addAttribute("vistaSupervisionAdmin", false);
        model.addAttribute("cursosTutor", cursos);
        return "tutor";
    }

    @GetMapping("/tutor-panel-estudiantes")
    public String verEstudiantes(
            Model model,
            @RequestParam(name = "filtroIdPersona", required = false) Integer filtroIdPersona,
            @RequestParam(name = "filtroIdTutor", required = false) Integer filtroIdTutor,
            @RequestParam(name = "filtroIdCurso", required = false) Integer filtroIdCurso) {
        Persona persona = getPersonaActual();
        if (persona == null) {
            return "redirect:/login";
        }
        model.addAttribute("persona", persona);

        if (esAdministrador()) {
            Integer filtroPersona = resolverFiltroIdPersona(filtroIdPersona, filtroIdTutor);
            List<Inscripcion> todas = inscripcionRepo.findAllForAdminTutorPanel();
            List<Inscripcion> filtradas = filtrarInscripcionesAdmin(todas, filtroPersona, filtroIdCurso);
            List<Curso> todosCursos = cursoRepo.findAllWithTutorPersonaOrderById();
            model.addAttribute("vistaSupervisionAdmin", true);
            model.addAttribute("inscripciones", filtradas);
            model.addAttribute("filtroIdPersona", filtroPersona);
            model.addAttribute("filtroIdCurso", filtroIdCurso);
            model.addAttribute("personasTutorFiltroSelect", personaRepo.findByRolIdOrderByNombreAsc(ROL_TUTOR));
            model.addAttribute("cursosFiltroSelect", todosCursos);
            return "tutor-estudiantes";
        }

        Tutor tutor = tutorRepo.findByPersona(persona).orElse(null);
        List<Inscripcion> inscripciones = (tutor != null)
                ? inscripcionRepo.findByCurso_Tutor_IdTutor(tutor.getIdTutor())
                : new ArrayList<>();

        model.addAttribute("vistaSupervisionAdmin", false);
        model.addAttribute("inscripciones", inscripciones);
        return "tutor-estudiantes";
    }

    /** Listado de cursos del tutor (enlaces del layout y post-login opcional). */
    @GetMapping("/tutor/panel/cursos")
    public String misCursosPanel(
            Model model,
            @RequestParam(name = "filtroIdPersona", required = false) Integer filtroIdPersona,
            @RequestParam(name = "filtroIdTutor", required = false) Integer filtroIdTutor,
            @RequestParam(name = "filtroIdCurso", required = false) Integer filtroIdCurso) {
        Persona persona = getPersonaActual();
        if (persona == null) {
            return "redirect:/login";
        }
        model.addAttribute("persona", persona);

        if (esAdministrador()) {
            Integer filtroPersona = resolverFiltroIdPersona(filtroIdPersona, filtroIdTutor);
            List<Curso> todosCursos = cursoRepo.findAllWithTutorPersonaOrderById();
            List<Curso> filtrados = filtrarCursosAdmin(todosCursos, filtroPersona, filtroIdCurso);
            model.addAttribute("vistaSupervisionAdmin", true);
            model.addAttribute("cursos", filtrados);
            model.addAttribute("filtroIdPersona", filtroPersona);
            model.addAttribute("filtroIdCurso", filtroIdCurso);
            model.addAttribute("personasTutorFiltroSelect", personaRepo.findByRolIdOrderByNombreAsc(ROL_TUTOR));
            model.addAttribute("cursosFiltroSelect", todosCursos);
            return "tutor-mis-cursos";
        }

        Tutor tutor = tutorRepo.findByPersona(persona).orElse(null);
        List<Curso> cursos = (tutor != null)
                ? cursoRepo.findByTutor_IdTutorWithTutorPersona(tutor.getIdTutor())
                : new ArrayList<>();
        model.addAttribute("vistaSupervisionAdmin", false);
        model.addAttribute("cursos", cursos);
        return "tutor-mis-cursos";
    }

    /**
     * Alumnos inscritos en un curso: tutor solo ve sus cursos; administrador ve cualquier curso.
     */
    @GetMapping("/tutor/panel/cursos/{idCurso}/alumnos")
    public String alumnosDelCurso(@PathVariable("idCurso") Integer idCurso, Model model) {
        Persona persona = getPersonaActual();
        if (persona == null) {
            return "redirect:/login";
        }
        model.addAttribute("persona", persona);

        if (esAdministrador()) {
            return cursoRepo.findByIdWithTutor(idCurso).map(curso -> {
                List<Inscripcion> inscripciones = inscripcionRepo.findByCursoIdWithEstudianteAndEstado(idCurso);
                Map<Integer, Integer> progresoPorInscripcion = new HashMap<>();
                for (Inscripcion i : inscripciones) {
                    progresoPorInscripcion.put(i.getId(),
                            progresoLeccionService.calcularPorcentaje(i.getEstudiante(), curso));
                }
                model.addAttribute("vistaSupervisionAdmin", true);
                model.addAttribute("curso", curso);
                model.addAttribute("inscripciones", inscripciones);
                model.addAttribute("progresoPorInscripcion", progresoPorInscripcion);
                return "tutor-curso-alumnos";
            }).orElse("redirect:/tutor/panel/cursos");
        }

        Tutor tutor = tutorRepo.findByPersona(persona).orElse(null);
        if (tutor == null) {
            return "redirect:/tutor-panel";
        }
        return cursoRepo.findByIdAndTutor_IdTutor(idCurso, tutor.getIdTutor()).map(curso -> {
            List<Inscripcion> inscripciones = inscripcionRepo.findByCursoIdWithEstudianteAndEstado(idCurso);
            Map<Integer, Integer> progresoPorInscripcion = new HashMap<>();
            for (Inscripcion i : inscripciones) {
                progresoPorInscripcion.put(i.getId(),
                        progresoLeccionService.calcularPorcentaje(i.getEstudiante(), curso));
            }
            model.addAttribute("vistaSupervisionAdmin", false);
            model.addAttribute("curso", curso);
            model.addAttribute("inscripciones", inscripciones);
            model.addAttribute("progresoPorInscripcion", progresoPorInscripcion);
            return "tutor-curso-alumnos";
        }).orElse("redirect:/tutor/panel/cursos");
    }

    private static boolean esAdministrador() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        for (GrantedAuthority a : auth.getAuthorities()) {
            if (ROLE_ADMIN.equals(a.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private Persona getPersonaActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails user) {
            return user.getPersona();
        }
        return null;
    }

    private static List<Curso> filtrarCursosAdmin(List<Curso> todos, Integer filtroIdPersona, Integer filtroIdCurso) {
        return todos.stream()
                .filter(c -> filtroIdPersona == null
                        || (c.getTutor() != null && c.getTutor().getPersona() != null
                                && filtroIdPersona.equals(c.getTutor().getPersona().getId())))
                .filter(c -> filtroIdCurso == null
                        || (c.getId() != null && filtroIdCurso.equals(c.getId())))
                .toList();
    }

    private static List<Curso> filtrarCursosLista(List<Curso> cursos, Integer filtroIdCurso) {
        if (filtroIdCurso == null) {
            return cursos;
        }
        return cursos.stream()
                .filter(c -> c.getId() != null && filtroIdCurso.equals(c.getId()))
                .toList();
    }

    private static List<Inscripcion> filtrarInscripcionesAdmin(
            List<Inscripcion> todas, Integer filtroIdPersona, Integer filtroIdCurso) {
        return todas.stream()
                .filter(i -> filtroIdPersona == null
                        || (i.getCurso() != null && i.getCurso().getTutor() != null
                                && i.getCurso().getTutor().getPersona() != null
                                && filtroIdPersona.equals(i.getCurso().getTutor().getPersona().getId())))
                .filter(i -> filtroIdCurso == null
                        || (i.getCurso() != null && i.getCurso().getId() != null
                                && filtroIdCurso.equals(i.getCurso().getId())))
                .toList();
    }

    /** Compatibilidad: {@code filtroIdTutor} antiguo se traduce a id de persona. */
    private Integer resolverFiltroIdPersona(Integer filtroIdPersona, Integer filtroIdTutor) {
        if (filtroIdPersona != null) {
            return filtroIdPersona;
        }
        if (filtroIdTutor == null) {
            return null;
        }
        return tutorRepo.findById(filtroIdTutor)
                .map(Tutor::getPersona)
                .map(Persona::getId)
                .orElse(null);
    }

    /**
     * Página de personas con rol tutor (3). Filtro por persona o por curso reduce a una fila.
     */
    private Page<Persona> paginaPersonasTutorAdmin(int page, Integer filtroPersona, Integer filtroIdCurso) {
        Pageable pageable = PageRequest.of(page, TUTOR_PAGE_SIZE);
        if (filtroPersona != null) {
            Optional<Persona> opt = personaRepo.findById(filtroPersona);
            if (opt.isPresent() && Integer.valueOf(ROL_TUTOR).equals(opt.get().getRolId())) {
                return new PageImpl<>(List.of(opt.get()), PageRequest.of(0, TUTOR_PAGE_SIZE), 1);
            }
            return new PageImpl<>(Collections.<Persona>emptyList(), pageable, 0);
        }
        if (filtroIdCurso != null) {
            return cursoRepo.findByIdWithTutor(filtroIdCurso)
                    .filter(c -> c.getTutor() != null && c.getTutor().getPersona() != null)
                    .map(c -> c.getTutor().getPersona())
                    .filter(p -> Integer.valueOf(ROL_TUTOR).equals(p.getRolId()))
                    .map(p -> new PageImpl<>(List.of(p), PageRequest.of(0, TUTOR_PAGE_SIZE), 1))
                    .orElseGet(() -> new PageImpl<>(Collections.<Persona>emptyList(), pageable, 0));
        }
        return personaRepo.findByRolIdOrderByNombreAsc(ROL_TUTOR, pageable);
    }
}
