package com.proyectojpa.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.proyectojpa.demo.Service.TutorService;
import com.proyectojpa.demo.domain.InscripcionEstados;
import com.proyectojpa.demo.models.Acudiente;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.models.Rol;
import com.proyectojpa.demo.repository.AcudienteRepository;
import com.proyectojpa.demo.repository.EstudianteRepository;
import com.proyectojpa.demo.repository.EstadoInscripcionRepository;
import com.proyectojpa.demo.repository.InscripcionRepository;
import com.proyectojpa.demo.repository.PasswordResetTokenRepository;
import com.proyectojpa.demo.repository.PersonaRepository;
import com.proyectojpa.demo.repository.ProgresoLeccionRepository;
import com.proyectojpa.demo.repository.ReciboRepository;
import com.proyectojpa.demo.repository.RolRepository;
import com.proyectojpa.demo.repository.TutorRepository;
import com.proyectojpa.demo.validation.OnAdminCreate;

import jakarta.validation.groups.Default;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * PersonaController maneja la gestión (CRUD) de usuarios del sistema desde el panel de administración.
 */
@Controller
@RequestMapping("/personas")
public class PersonaController {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SmartValidator validator;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private EstadoInscripcionRepository estadoInscripcionRepository;

    @Autowired
    private AcudienteRepository acudienteRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private ProgresoLeccionRepository progresoLeccionRepository;

    @Autowired
    private ReciboRepository reciboRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private TutorService tutorService;

    private void cargarRoles(Model model) {
        List<Rol> roles = rolRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        model.addAttribute("roles", roles);
    }

    private static void normalizarEmail(Persona persona) {
        if (persona.getEmail() != null) {
            persona.setEmail(persona.getEmail().trim().toLowerCase(Locale.ROOT));
        }
    }

    /** Validación acudiente para TI (misma regla que registro público). */
    private String validarAcudienteTi(Persona persona) {
        if (persona.getRolId() == null || persona.getRolId() != 2) {
            return "Con Tarjeta de identidad el rol debe ser Estudiante para registrar al acudiente.";
        }
        if (persona.getTutorTipoDocumento() == null || persona.getTutorTipoDocumento().isBlank()) {
            return "Seleccione el tipo de documento del acudiente.";
        }
        if (!"CC".equals(persona.getTutorTipoDocumento()) && !"CE".equals(persona.getTutorTipoDocumento())) {
            return "Tipo de documento del acudiente no válido (use CC o CE).";
        }
        if (persona.getTutorDocumento() == null || persona.getTutorDocumento().trim().isEmpty()) {
            return "El documento del acudiente es obligatorio.";
        }
        if (!persona.getTutorDocumento().matches("\\d{5,}")) {
            return "El documento del acudiente debe ser numérico (mínimo 5 dígitos).";
        }
        if (personaRepository.findByDocumento(persona.getTutorDocumento()) != null) {
            return "El documento del acudiente ya está registrado.";
        }
        if (persona.getDocumento() != null && persona.getDocumento().equals(persona.getTutorDocumento())) {
            return "El estudiante y el acudiente no pueden tener el mismo documento.";
        }
        if (persona.getTutorNombre() == null || persona.getTutorNombre().trim().isEmpty()) {
            return "El nombre del acudiente es obligatorio.";
        }
        if (!persona.getTutorNombre().matches("[A-Za-zÁÉÍÓÚáéíóúñÑüÜ\\s]+")) {
            return "El nombre del acudiente solo puede contener letras.";
        }
        if (persona.getTutorGenero() == null || persona.getTutorGenero().isBlank()) {
            return "Seleccione el género del acudiente.";
        }
        if (persona.getTutorAutorizacion() == null || persona.getTutorAutorizacion().isBlank()) {
            return "Seleccione parentesco / autorización del acudiente.";
        }
        if (persona.getTutorDireccion() == null || persona.getTutorDireccion().trim().isEmpty()) {
            return "La dirección del acudiente es obligatoria.";
        }
        if (persona.getTutorTelefono() == null || !persona.getTutorTelefono().matches("\\d{7,10}")) {
            return "El teléfono del acudiente debe tener entre 7 y 10 dígitos numéricos.";
        }
        if (persona.getTutorEmail() == null || persona.getTutorEmail().trim().isEmpty()) {
            return "El correo del acudiente es obligatorio.";
        }
        persona.setTutorEmail(persona.getTutorEmail().trim().toLowerCase(Locale.ROOT));
        if (personaRepository.findByEmail(persona.getTutorEmail()) != null) {
            return "El correo del acudiente ya está registrado.";
        }
        return null;
    }

    @GetMapping
    public String mostrarPersonas(Model model) {
        List<Persona> personas = personaRepository.findAll();
        model.addAttribute("personas", personas);
        return "lista";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNuevaPersona(Model model) {
        model.addAttribute("persona", new Persona());
        cargarRoles(model);
        return "formulario";
    }

    @Transactional
    @PostMapping
    public String guardarPersona(
            @ModelAttribute("persona") Persona persona,
            BindingResult result,
            Model model) {

        normalizarEmail(persona);
        validator.validate(persona, result, Default.class, OnAdminCreate.class);

        Persona dupDoc = personaRepository.findByDocumento(persona.getDocumento());
        if (dupDoc != null) {
            result.rejectValue("documento", "duplicate.documento", "Ya existe una persona con ese documento");
        }
        Persona dupEmail = personaRepository.findByEmail(persona.getEmail());
        if (dupEmail != null) {
            result.rejectValue("email", "duplicate.email", "El correo ya está registrado");
        }

        boolean errorAcudiente = false;
        if ("TI".equals(persona.getTipoDocumento())) {
            String errTutor = validarAcudienteTi(persona);
            if (errTutor != null) {
                model.addAttribute("errorTutor", errTutor);
                errorAcudiente = true;
            }
        }

        if (result.hasErrors() || errorAcudiente) {
            cargarRoles(model);
            return "formulario";
        }

        String passCifrada = passwordEncoder.encode(persona.getContrasena());
        persona.setContrasena(passCifrada);
        Persona guardada = personaRepository.save(persona);

        // Solo rol estudiante (2) tiene fila en tabla estudiante; inscripciones cuelgan del estudiante.
        if (guardada.getRolId() != null && guardada.getRolId() == 2) {
            Estudiante estudiante = new Estudiante();
            estudiante.setPersona(guardada);
            estudiante.setProgreso("0%");
            estadoInscripcionRepository.findByCodigo(InscripcionEstados.ACTIVO)
                    .ifPresent(estudiante::setEstadoEstudiante);
            Estudiante estudianteGuardado = estudianteRepository.save(estudiante);

            if ("TI".equals(guardada.getTipoDocumento())) {
                Persona acudientePersona = new Persona();
                acudientePersona.setTipoDocumento(persona.getTutorTipoDocumento());
                acudientePersona.setDocumento(persona.getTutorDocumento());
                acudientePersona.setGenero(persona.getTutorGenero());
                acudientePersona.setNombre(persona.getTutorNombre());
                acudientePersona.setDireccion(persona.getTutorDireccion());
                acudientePersona.setTelefono(persona.getTutorTelefono());
                acudientePersona.setEmail(persona.getTutorEmail());
                acudientePersona.setContrasena(passCifrada);
                acudientePersona.setRolId(4);
                acudientePersona.setEstudiantes(new ArrayList<>());
                Persona acudienteGuardado = personaRepository.save(acudientePersona);

                Acudiente vinculacion = new Acudiente();
                vinculacion.setPersona(acudienteGuardado);
                vinculacion.setEstudianteDependiente(estudianteGuardado);
                vinculacion.setParentesco(persona.getTutorAutorizacion());
                acudienteRepository.save(vinculacion);
            }
        }

        return "redirect:/admin";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + id));
        persona.setContrasena("");
        model.addAttribute("persona", persona);
        if (persona.getRolId() != null && persona.getRolId() == 3) {
            tutorRepository.findByPersona(persona).ifPresent(t -> model.addAttribute("tutor", t));
        }
        cargarRoles(model);
        return "formulario";
    }

    @PostMapping("/{id}")
    public String actualizarPersona(
            @PathVariable Integer id,
            @ModelAttribute("persona") Persona persona,
            BindingResult result,
            Model model,
            @RequestParam(required = false) MultipartFile imagenTutorArchivo) {

        Persona existente = personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + id));

        normalizarEmail(persona);

        String nuevaPass = persona.getContrasena();
        boolean cambiaPass = nuevaPass != null && !nuevaPass.isBlank();
        if (!cambiaPass) {
            persona.setContrasena("");
        }

        persona.setId(id);
        validator.validate(persona, result, Default.class);

        Persona dupDoc = personaRepository.findByDocumento(persona.getDocumento());
        if (dupDoc != null && !dupDoc.getId().equals(id)) {
            result.rejectValue("documento", "duplicate.documento", "Ya existe una persona con ese documento");
        }
        Persona dupEmail = personaRepository.findByEmail(persona.getEmail());
        if (dupEmail != null && !dupEmail.getId().equals(id)) {
            result.rejectValue("email", "duplicate.email", "El correo ya está registrado");
        }

        if (result.hasErrors()) {
            persona.setContrasena("");
            if (persona.getRolId() != null && persona.getRolId() == 3) {
                tutorRepository.findByPersona(persona).ifPresent(t -> model.addAttribute("tutor", t));
            }
            cargarRoles(model);
            return "formulario";
        }

        if (!cambiaPass) {
            persona.setContrasena(existente.getContrasena());
        } else {
            persona.setContrasena(passwordEncoder.encode(nuevaPass));
        }

        personaRepository.save(persona);

        if (persona.getRolId() != null && persona.getRolId() == 3
                && imagenTutorArchivo != null && !imagenTutorArchivo.isEmpty()) {
            try {
                tutorService.aplicarFotoTutorSiEnviada(id, imagenTutorArchivo);
            } catch (Exception e) {
                model.addAttribute("errorFotoTutor", e.getMessage());
                persona.setContrasena("");
                tutorRepository.findByPersona(persona).ifPresent(t -> model.addAttribute("tutor", t));
                cargarRoles(model);
                return "formulario";
            }
        }

        return "redirect:/admin";
    }

    /**
     * Quita inscripciones (y recibos), progreso y vínculos {@link Acudiente} que apuntan a este id de estudiante.
     */
    private void eliminarDatosLigadosAEstudiante(Integer idEstudiante) {
        if (idEstudiante == null) {
            return;
        }
        acudienteRepository.deleteAll(acudienteRepository.findByEstudianteDependienteIdEstudiante(idEstudiante));
        reciboRepository.deleteByInscripcion_Estudiante_IdEstudiante(idEstudiante);
        inscripcionRepository.deleteByEstudiante_IdEstudiante(idEstudiante);
        progresoLeccionRepository.deleteByEstudiante_IdEstudiante(idEstudiante);
    }

    /**
     * Borrado en cascada desde el panel admin: tokens, perfiles estudiante (y datos relacionados),
     * filas en {@code acudiente} donde esta persona es el acudiente, tutor si aplica, y la persona.
     */
    @Transactional
    @GetMapping("/eliminar/{id}")
    public String eliminarPersona(@PathVariable Integer id) {
        Persona persona = personaRepository.findById(id).orElse(null);
        if (persona == null) {
            return "redirect:/admin";
        }

        passwordResetTokenRepository.deleteByPersona_Id(id);

        for (Estudiante e : estudianteRepository.findAllByPersonaOrderByIdEstudianteAsc(persona)) {
            Integer idEst = e.getIdEstudiante();
            eliminarDatosLigadosAEstudiante(idEst);
            estudianteRepository.deleteById(idEst);
        }

        acudienteRepository.deleteAll(acudienteRepository.findByPersonaId(id));

        if (persona.getRolId() != null && persona.getRolId() == 3) {
            tutorRepository.findByPersona(persona).ifPresent(tutorRepository::delete);
        }

        personaRepository.deleteById(id);
        return "redirect:/admin";
    }
}
