package com.proyectojpa.demo.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectojpa.demo.Service.FileStorageService;
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
import com.proyectojpa.demo.util.YoutubeUrlUtil;

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

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/{id}/contenido")
    @Transactional(readOnly = true)
    public String gestionarContenido(@PathVariable("id") Integer id, Model model, Authentication auth) {
        assertPuedeGestionarContenido(id, auth);
        Curso curso = cursoRepo.findByIdWithContenido(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + id));

        if (curso.getModulos() != null) {
            for (Modulo modulo : curso.getModulos()) {
                if (modulo != null && modulo.getLecciones() != null) {
                    modulo.getLecciones().size();
                }
            }
        }

        model.addAttribute("curso", curso);
        model.addAttribute("nuevoModulo", new Modulo());
        model.addAttribute("nuevaLeccion", new Leccion());

        return "gestion-contenido";
    }

    @PostMapping("/{id}/modulos")
    public String agregarModulo(@PathVariable("id") Integer id, @ModelAttribute Modulo modulo, Authentication auth) {
        assertPuedeGestionarContenido(id, auth);
        Curso curso = cursoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + id));

        modulo.setCurso(curso);
        moduloRepo.save(modulo);

        return "redirect:/admin/cursos/" + id + "/contenido";
    }

    @PostMapping(value = "/modulos/{moduloId}/lecciones", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String agregarLeccion(@PathVariable("moduloId") Integer moduloId,
            @RequestParam String nombre,
            @RequestParam String contenidoTipo,
            @RequestParam(required = false) String contenidoUrl,
            @RequestParam(required = false) String contenidoTexto,
            @RequestParam(required = false) Integer cantidad,
            @RequestParam(required = false) MultipartFile archivoPdf,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        Integer cursoId = moduloRepo.findCursoIdByModuloId(moduloId).orElse(null);
        if (cursoId == null) {
            redirectAttributes.addFlashAttribute("errorLeccion", "Módulo no encontrado.");
            return "redirect:/admin";
        }
        try {
            assertPuedeGestionarContenido(cursoId, auth);
            Modulo modulo = moduloRepo.findById(moduloId)
                    .orElseThrow(() -> new IllegalArgumentException("Módulo no encontrado: " + moduloId));

            Leccion leccion = new Leccion();
            leccion.setNombre(nombre.trim());
            leccion.setContenidoTipo(contenidoTipo.trim());
            leccion.setCantidad(cantidad);
            leccion.setModulo(modulo);

            String tipo = leccion.getContenidoTipo();
            if ("video".equals(tipo)) {
                if (contenidoUrl == null || contenidoUrl.isBlank()) {
                    throw new IllegalArgumentException("Indique la URL de YouTube.");
                }
                if (!YoutubeUrlUtil.pareceUrlYoutube(contenidoUrl.trim())) {
                    throw new IllegalArgumentException("La URL debe ser de YouTube (youtube.com o youtu.be).");
                }
                if (YoutubeUrlUtil.extractVideoId(contenidoUrl.trim()) == null) {
                    throw new IllegalArgumentException("No se pudo reconocer el video de YouTube en la URL.");
                }
                leccion.setContenidoUrl(contenidoUrl.trim());
                leccion.setContenidoTexto(null);
            } else if ("texto".equals(tipo)) {
                if (contenidoTexto == null || contenidoTexto.isBlank()) {
                    throw new IllegalArgumentException("Escriba el texto de la lección de lectura.");
                }
                leccion.setContenidoTexto(contenidoTexto);
                leccion.setContenidoUrl(null);
            } else if ("pdf".equals(tipo)) {
                if (archivoPdf == null || archivoPdf.isEmpty()) {
                    throw new IllegalArgumentException("Seleccione un archivo PDF.");
                }
                String ruta = fileStorageService.guardarPdfLeccion(archivoPdf);
                leccion.setContenidoUrl(ruta);
                leccion.setContenidoTexto(null);
            } else {
                throw new IllegalArgumentException("Tipo de contenido no válido.");
            }

            leccionRepo.save(leccion);
            return "redirect:/admin/cursos/" + cursoId + "/contenido";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorLeccion", ex.getMessage());
            return "redirect:/admin/cursos/" + cursoId + "/contenido";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorLeccion", "No se pudo guardar el PDF: " + e.getMessage());
            return "redirect:/admin/cursos/" + cursoId + "/contenido";
        }
    }

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

    @GetMapping("/lecciones/eliminar/{id}")
    public String eliminarLeccion(@PathVariable("id") Integer id, Authentication auth) {
        Integer cursoId = leccionRepo.findCursoIdByLeccionId(id)
                .orElseThrow(() -> new IllegalArgumentException("Lección no encontrada: " + id));
        assertPuedeGestionarContenido(cursoId, auth);
        Leccion leccion = leccionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lección no encontrada: " + id));
        if ("pdf".equals(leccion.getContenidoTipo()) && leccion.getContenidoUrl() != null) {
            fileStorageService.eliminarSiRutaPdfLeccion(leccion.getContenidoUrl());
        }
        leccionRepo.delete(leccion);
        return "redirect:/admin/cursos/" + cursoId + "/contenido";
    }

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
