package com.proyectojpa.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.repository.PersonaRepository;
import com.proyectojpa.demo.repository.EstudianteRepository;
import com.proyectojpa.demo.security.CustomUserDetails;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @GetMapping
    public String verPerfil(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        // Obtenemos la persona actualizada desde la base de datos
        Persona persona = personaRepository.findById(userDetails.getPersona().getId())
                .orElse(userDetails.getPersona());
        
        // Buscamos si tiene perfil de estudiante para mostrar datos de tutor
        Estudiante estudiante = estudianteRepository.findByPersona(persona).orElse(null);

        model.addAttribute("persona", persona);
        model.addAttribute("estudiante", estudiante);
        return "perfil";
    }

    @PostMapping("/actualizar")
    public String actualizarPerfil(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @ModelAttribute Persona personaForm,
                                   @RequestParam(required = false) String tutorNombre,
                                   @RequestParam(required = false) String tutorTelefono,
                                   @RequestParam(required = false) String tutorEmail,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Buscamos la persona original
            Persona personaExistente = personaRepository.findById(userDetails.getPersona().getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            personaExistente.setNombre(personaForm.getNombre());
            personaExistente.setTelefono(personaForm.getTelefono());
            personaExistente.setDireccion(personaForm.getDireccion());

            personaRepository.save(personaExistente);

            // Actualizar tutor si es estudiante
            Estudiante estudiante = estudianteRepository.findByPersona(personaExistente).orElse(null);

            if (estudiante != null) {
                estudiante.setTutorNombre(tutorNombre);
                estudiante.setTutorTelefono(tutorTelefono);
                estudiante.setTutorEmail(tutorEmail);
                estudianteRepository.save(estudiante);
            }

            redirectAttributes.addFlashAttribute("mensaje", "¡Perfil actualizado con éxito!");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar perfil: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/perfil";
    }
}
