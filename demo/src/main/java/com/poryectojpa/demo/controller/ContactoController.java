package com.poryectojpa.demo.controller;

import com.poryectojpa.demo.models.Contacto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ContactoController {

    @GetMapping("/contacto")
    public String mostrarFormulario(Model model) {
        model.addAttribute("contacto", new Contacto()); // objeto vacío
        return "contacto"; // contacto.html en templates
    }

    @PostMapping("/contacto")
    public String enviarFormulario(
            @Valid @ModelAttribute("contacto") Contacto contacto,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "contacto"; // vuelve al formulario con errores
        }

        // Aquí podrías guardar en BD o enviar correo...
        model.addAttribute("exito", "Tu mensaje ha sido enviado correctamente ✅");
        return "contacto";
    }
}
