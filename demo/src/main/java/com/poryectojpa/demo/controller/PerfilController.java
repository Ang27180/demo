package com.poryectojpa.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.poryectojpa.demo.models.Persona;
import com.poryectojpa.demo.repository.personaRepository;
import com.poryectojpa.demo.security.CustomUserDetails;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private personaRepository personaRepository;

    @GetMapping
    public String verPerfil(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        // Obtenemos la persona actualizada desde la base de datos
        Persona persona = personaRepository.findById(userDetails.getPersona().getId())
                .orElse(userDetails.getPersona());
        
        model.addAttribute("persona", persona);
        return "perfil";
    }

    @PostMapping("/actualizar")
    public String actualizarPerfil(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @ModelAttribute Persona personaForm,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Buscamos la persona original para no perder datos sensibles como contraseña o rol
            Persona personaExistente = personaRepository.findById(userDetails.getPersona().getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Solo permitimos actualizar ciertos campos por seguridad
            personaExistente.setNombre(personaForm.getNombre());
            personaExistente.setTelefono(personaForm.getTelefono());
            personaExistente.setDireccion(personaForm.getDireccion());
            // El email suele ser el identificador, así que normalmente no se cambia así de fácil
            // personaExistente.setEmail(personaForm.getEmail()); 

            personaRepository.save(personaExistente);

            redirectAttributes.addFlashAttribute("mensaje", "¡Perfil actualizado con éxito!");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar perfil: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/perfil";
    }
}
