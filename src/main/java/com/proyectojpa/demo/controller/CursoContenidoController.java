package com.proyectojpa.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.proyectojpa.demo.models.Curso;
import com.proyectojpa.demo.models.Leccion;
import com.proyectojpa.demo.models.Modulo;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.models.Tutor;
import com.proyectojpa.demo.repository.LeccionRepository;
import com.proyectojpa.demo.repository.ModuloRepository;
import com.proyectojpa.demo.repository.TutorRepository;
import com.proyectojpa.demo.repository.cursoRepository;
import com.proyectojpa.demo.security.CustomUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/cursos")
public class CursoContenidoController {

    @Autowired
    private cursoRepository cursoRepo;

    @Autowired
    private ModuloRepository moduloRepo;

    @Autowired
    private LeccionRepository leccionRepo;

    @Autowired
    private TutorRepository tutorRepository;

    // --- AJUSTE: Gestionar contenido de un curso (Módulos y Lecciones) ---
    @GetMapping("/{id}/contenido")
    @Transactional(readOnly = true)
    public String gestionarContenido(@PathVariable("id") Integer id, Model model, Authentication auth) {
        assertPuedeGestionarContenido(id, auth);
        Curso curso = cursoRepo.findByIdWithContenido(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + id));

        // open-in-view=false: inicializamos `lecciones` mientras la sesión/transacción sigue activa
        if (curso.getModulos() != null) {
            for (Modulo modulo : curso.getModulos()) {
                if (modulo != null && modulo.getLecciones() != null) {
                    modulo.getLecciones().size(); // fuerza carga lazy
                }
            }
        }
        
        model.addAttribute("curso", curso);
        model.addAttribute("nuevoModulo", new Modulo());
        model.addAttribute("nuevaLeccion", new Leccion());
        
        return "gestion-contenido";
    }

    // AJUSTE: Agregar un nuevo módulo
    @PostMapping("/{id}/modulos")
    public String agregarModulo(@PathVariable("id") Integer id, @ModelAttribute Modulo modulo, Authentication auth) {
        assertPuedeGestionarContenido(id, auth);
        Curso curso = cursoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + id));
        
        modulo.setCurso(curso);
        moduloRepo.save(modulo);
        
        return "redirect:/admin/cursos/" + id + "/contenido";
    }

    // AJUSTE: Agregar una nueva lección a un módulo
    @PostMapping("/modulos/{moduloId}/lecciones")
    public String agregarLeccion(@PathVariable("moduloId") Integer moduloId, @ModelAttribute Leccion leccion,
            Authentication auth) {
        Integer cursoId = moduloRepo.findCursoIdByModuloId(moduloId)
                .orElseThrow(() -> new IllegalArgumentException("Módulo no encontrado: " + moduloId));
        assertPuedeGestionarContenido(cursoId, auth);
        Modulo modulo = moduloRepo.findById(moduloId)
                .orElseThrow(() -> new IllegalArgumentException("Módulo no encontrado: " + moduloId));
        
        leccion.setModulo(modulo);
        leccionRepo.save(leccion);
        
        return "redirect:/admin/cursos/" + modulo.getCurso().getId() + "/contenido";
    }

    // AJUSTE: Eliminar un módulo
    @GetMapping("/modulos/eliminar/{id}")
    public String eliminarModulo(@PathVariable("id") Integer id, Authentication auth) {
        Integer cursoId = moduloRepo.findCursoIdByModuloId(id)
                .orElseThrow(() -> new IllegalArgumentException("Módulo no encontrado: " + id));
        assertPuedeGestionarContenido(cursoId, auth);
        Modulo modulo = moduloRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Módulo no encontrado: " + id));
        moduloRepo.delete(modulo);
        return "redirect:/admin/cursos/" + cursoId + "/contenido";
    }

    // AJUSTE: Eliminar una lección
    @GetMapping("/lecciones/eliminar/{id}")
    public String eliminarLeccion(@PathVariable("id") Integer id, Authentication auth) {
        Integer cursoId = leccionRepo.findCursoIdByLeccionId(id)
                .orElseThrow(() -> new IllegalArgumentException("Lección no encontrada: " + id));
        assertPuedeGestionarContenido(cursoId, auth);
        Leccion leccion = leccionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lección no encontrada: " + id));
        leccionRepo.delete(leccion);
        return "redirect:/admin/cursos/" + cursoId + "/contenido";
    }

    /**
     * Admin: cualquier curso. Tutor: solo cursos donde es el tutor asignado.
     */
    private void assertPuedeGestionarContenido(Integer idCurso, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        boolean esAdmin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (esAdmin) {
            return;
        }
        boolean esTutor = auth.getAuthorities().stream().anyMatch(a -> "ROLE_TUTOR".equals(a.getAuthority()));
        if (!esTutor) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado a gestionar contenido");
        }
        if (!(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Persona persona = userDetails.getPersona();
        Tutor tutor = tutorRepository.findByPersona(persona).orElse(null);
        if (tutor == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil tutor no encontrado");
        }
        if (cursoRepo.findByIdAndTutor_IdTutor(idCurso, tutor.getIdTutor()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este curso no está asignado a tu perfil tutor");
        }
    }
}
