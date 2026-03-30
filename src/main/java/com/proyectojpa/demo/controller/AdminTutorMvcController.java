package com.proyectojpa.demo.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectojpa.demo.Service.FileStorageService;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.models.Tutor;
import com.proyectojpa.demo.repository.PersonaRepository;
import com.proyectojpa.demo.repository.TutorRepository;

/**
 * Gestión de tutores (vistas HTML para administración).
 */
@Controller
@RequestMapping("/admin/tutores")
public class AdminTutorMvcController {

    private final TutorRepository tutorRepository;
    private final PersonaRepository personaRepository;
    private final FileStorageService fileStorageService;

    public AdminTutorMvcController(TutorRepository tutorRepository, PersonaRepository personaRepository,
            FileStorageService fileStorageService) {
        this.tutorRepository = tutorRepository;
        this.personaRepository = personaRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tutores", tutorRepository.findAllWithPersona());
        return "admin-tutores";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("tutor", new Tutor());
        model.addAttribute("personasDisponibles", personasDisponiblesParaFormulario(null));
        return "admin-tutor-form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        Tutor t = tutorRepository.findByIdWithPersona(id)
                .orElseThrow(() -> new IllegalArgumentException("Tutor no encontrado"));
        model.addAttribute("tutor", t);
        model.addAttribute("personasDisponibles", personasDisponiblesParaFormulario(id));
        return "admin-tutor-form";
    }

    private List<Persona> personasDisponiblesParaFormulario(Integer idTutorEditando) {
        List<Persona> rolTutor = personaRepository.findAll().stream()
                .filter(p -> p.getRolId() != null && p.getRolId() == 3)
                .collect(Collectors.toList());
        if (idTutorEditando == null) {
            return rolTutor.stream()
                    .filter(p -> tutorRepository.findByPersona(p).isEmpty())
                    .collect(Collectors.toList());
        }
        Tutor actual = tutorRepository.findById(idTutorEditando).orElseThrow();
        Persona pActual = actual.getPersona();
        return rolTutor.stream()
                .filter(p -> tutorRepository.findByPersona(p).isEmpty()
                        || (pActual != null && p.getId().equals(pActual.getId())))
                .collect(Collectors.toList());
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam(required = false) Integer idTutor,
            @RequestParam Integer idPersona,
            @RequestParam(required = false) String experiencia,
            @RequestParam(required = false) String observaciones,
            @RequestParam(required = false) MultipartFile imagenArchivo,
            RedirectAttributes redirectAttributes) {
        try {
            Persona persona = personaRepository.findById(idPersona)
                    .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada"));
            if (persona.getRolId() == null || persona.getRolId() != 3) {
                redirectAttributes.addFlashAttribute("errorAdmin", "La persona debe tener rol Tutor (3).");
                return "redirect:/admin/tutores";
            }
            if (idTutor == null) {
                tutorRepository.findByPersona(persona).ifPresent(otro -> {
                    throw new IllegalStateException("Esa persona ya tiene un perfil de tutor.");
                });
            } else {
                tutorRepository.findByPersona(persona).ifPresent(otro -> {
                    if (!otro.getIdTutor().equals(idTutor)) {
                        throw new IllegalStateException("Esa persona ya está asignada a otro tutor.");
                    }
                });
            }
            Tutor tutor = idTutor != null ? tutorRepository.findById(idTutor).orElse(new Tutor()) : new Tutor();
            if (idTutor != null) {
                tutor.setIdTutor(idTutor);
            }
            tutor.setPersona(persona);
            tutor.setExperiencia(experiencia);
            tutor.setObservaciones(observaciones);

            if (imagenArchivo != null && !imagenArchivo.isEmpty()) {
                String nueva = fileStorageService.guardarFotoTutor(imagenArchivo);
                fileStorageService.eliminarSiRutaFotoTutor(tutor.getImagen());
                tutor.setImagen(nueva);
            }

            tutorRepository.save(tutor);
            redirectAttributes.addFlashAttribute("msgAdmin", "Tutor guardado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorAdmin", e.getMessage());
        }
        return "redirect:/admin/tutores";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Tutor> opt = tutorRepository.findById(id);
            if (opt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorAdmin", "Tutor no encontrado.");
                return "redirect:/admin/tutores";
            }
            Tutor t = opt.get();
            fileStorageService.eliminarSiRutaFotoTutor(t.getImagen());
            tutorRepository.delete(t);
            redirectAttributes.addFlashAttribute("msgAdmin", "Tutor eliminado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorAdmin", "No se pudo eliminar: " + e.getMessage());
        }
        return "redirect:/admin/tutores";
    }
}
