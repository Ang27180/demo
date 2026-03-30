package com.proyectojpa.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.proyectojpa.demo.domain.InscripcionEstados;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.PersonaRepository;
import com.proyectojpa.demo.validation.OnRegistro;

import jakarta.validation.groups.Default;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class RegistroController {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.proyectojpa.demo.repository.EstudianteRepository estudianteRepository;

    @Autowired
    private com.proyectojpa.demo.repository.EstadoInscripcionRepository estadoInscripcionRepository;

    @Autowired
    private com.proyectojpa.demo.repository.AcudienteRepository acudienteRepository;

    @Autowired
    private SmartValidator validator;

    @GetMapping("/registro")
    public String mostrarFormulario(Model model) {
        Persona persona = new Persona();
        persona.setRolId(2); // estudiante (formulario público: siempre rol 2)
        model.addAttribute("persona", persona);
        return "registro"; // vista registro.html
    }

    @Transactional
    @PostMapping("/registro")
    public String procesarFormulario(
            @ModelAttribute("persona") Persona persona,
            BindingResult result,
            Model model) {

        // Rol fijo antes de validar (@NotNull en rolId)
        persona.setRolId(2);
        if (persona.getEmail() != null) {
            persona.setEmail(persona.getEmail().trim().toLowerCase(Locale.ROOT));
        }
        validator.validate(persona, result, Default.class, OnRegistro.class);
        if (result.hasErrors()) {
            return "registro";
        }

        // Validar si el correo ya existe
        if (personaRepository.findByEmail(persona.getEmail()) != null) {
            model.addAttribute("errorCorreo", "El correo ya está registrado");
            return "registro";
        }

        // Validar confirmación de contraseña
        if (!persona.getContrasena().equals(persona.getConfirmarContrasena())) {
            model.addAttribute("errorContrasena", "Las contraseñas no coinciden");
            return "registro";
        }

        // Validar aceptación de términos
        if (persona.getAceptaTerminos() == null || !persona.getAceptaTerminos()) {
            model.addAttribute("errorTerminos", "Debes aceptar los términos y condiciones");
            return "registro";
        }

        // Validación específica para TI (Acudiente)
        if ("TI".equals(persona.getTipoDocumento())) {
            if (persona.getTutorDocumento() == null || persona.getTutorDocumento().trim().isEmpty()) {
                model.addAttribute("errorTutor", "Los datos del acudiente son requeridos para TI.");
                return "registro";
            }
            if (!persona.getTutorDocumento().matches("\\d{5,}")) {
                model.addAttribute("errorTutor", "El documento del acudiente debe ser numérico (mínimo 5 dígitos).");
                return "registro";
            }
            if (personaRepository.findByDocumento(persona.getTutorDocumento()) != null) {
                model.addAttribute("errorTutor", "El documento del acudiente ya está registrado.");
                return "registro";
            }
            // Validar que estudiante y acudiente no tengan el mismo documento
            if (persona.getDocumento() != null && persona.getDocumento().equals(persona.getTutorDocumento())) {
                model.addAttribute("errorTutor", "El estudiante y el acudiente no pueden tener el mismo número de documento.");
                return "registro";
            }
            // Validar nombre del acudiente
            if (persona.getTutorNombre() == null || persona.getTutorNombre().trim().isEmpty()) {
                model.addAttribute("errorTutor", "El nombre del acudiente es obligatorio.");
                return "registro";
            }
            if (!persona.getTutorNombre().matches("[A-Za-zÁÉÍÓÚáéíóúñÑüÜ\\s]+")) {
                model.addAttribute("errorTutor", "El nombre del acudiente solo puede contener letras.");
                return "registro";
            }
            // Validar teléfono del acudiente
            if (persona.getTutorTelefono() == null || !persona.getTutorTelefono().matches("\\d{7,10}")) {
                model.addAttribute("errorTutor", "El teléfono del acudiente debe ser de 7 a 10 dígitos numéricos.");
                return "registro";
            }
            if (persona.getTutorAutorizacion() == null || persona.getTutorAutorizacion().isBlank()) {
                model.addAttribute("errorTutor", "Seleccione parentesco o autorización del acudiente.");
                return "registro";
            }
            if (persona.getTutorDireccion() == null || persona.getTutorDireccion().trim().isEmpty()) {
                model.addAttribute("errorTutor", "La dirección del acudiente es obligatoria.");
                return "registro";
            }
            if (persona.getTutorGenero() == null || persona.getTutorGenero().isBlank()) {
                model.addAttribute("errorTutor", "Seleccione el género del acudiente.");
                return "registro";
            }
            if (persona.getTutorTipoDocumento() == null || (!"CC".equals(persona.getTutorTipoDocumento()) && !"CE".equals(persona.getTutorTipoDocumento()))) {
                model.addAttribute("errorTutor", "Tipo de documento del acudiente no válido (use CC o CE).");
                return "registro";
            }
            if (persona.getTutorEmail() == null || persona.getTutorEmail().trim().isEmpty()) {
                model.addAttribute("errorTutor", "El correo del acudiente es obligatorio.");
                return "registro";
            }
            persona.setTutorEmail(persona.getTutorEmail().trim().toLowerCase(Locale.ROOT));
            if (personaRepository.findByEmail(persona.getTutorEmail()) != null) {
                model.addAttribute("errorTutor", "El correo del acudiente ya está registrado.");
                return "registro";
            }
        }

        // Cifrar la contraseña antes de guardar (se usará para ambos si es TI)
        String passCifrada = passwordEncoder.encode(persona.getContrasena());
        persona.setContrasena(passCifrada);

        // Guardar persona en la base de datos
        Persona guardada = personaRepository.save(persona);

        // AJUSTE: Si el rol es Usuario (2), creamos automáticamente su perfil de estudiante
        if (guardada.getRolId() != null && guardada.getRolId() == 2) {
            com.proyectojpa.demo.models.Estudiante estudiante = new com.proyectojpa.demo.models.Estudiante();
            estudiante.setPersona(guardada);
            estudiante.setProgreso("0%");
            estadoInscripcionRepository.findByCodigo(InscripcionEstados.ACTIVO)
                    .ifPresent(estudiante::setEstadoEstudiante);
            com.proyectojpa.demo.models.Estudiante estudianteGuardado = estudianteRepository.save(estudiante);

            // ✅ Si es TI, creamos el Acudiente como Persona independiente
            if ("TI".equals(guardada.getTipoDocumento())) {
                Persona acudientePersona = new Persona();
                // Usamos 'persona' (el objeto del formulario) para acceder a los @Transient
                acudientePersona.setTipoDocumento(persona.getTutorTipoDocumento());
                acudientePersona.setDocumento(persona.getTutorDocumento());
                acudientePersona.setGenero(persona.getTutorGenero());
                acudientePersona.setNombre(persona.getTutorNombre());
                acudientePersona.setDireccion(persona.getTutorDireccion());
                acudientePersona.setTelefono(persona.getTutorTelefono());
                acudientePersona.setEmail(persona.getTutorEmail());
                acudientePersona.setContrasena(passCifrada); // Misma contraseña
                acudientePersona.setRolId(4); // Rol Acudiente
                acudientePersona.setEstudiantes(new ArrayList<>());

                Persona acudienteGuardado = personaRepository.save(acudientePersona);

                // Vincular en la tabla acudiente
                com.proyectojpa.demo.models.Acudiente vinculacion = new com.proyectojpa.demo.models.Acudiente();
                vinculacion.setPersona(acudienteGuardado);
                vinculacion.setEstudianteDependiente(estudianteGuardado);
                vinculacion.setParentesco(persona.getTutorAutorizacion());
                acudienteRepository.save(vinculacion);
            }
        }

        // Redirigir al login
        return "redirect:/login";
    }
}
