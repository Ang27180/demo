package com.proyectojpa.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.proyectojpa.demo.Service.PasswordResetTokenService;

@Controller
public class ResetPasswordController {

    private static final Logger log = LoggerFactory.getLogger(ResetPasswordController.class);

    private static final String MENSAJE_ERROR_TITULO = "No se pudo validar el enlace";
    private static final String MENSAJE_ERROR_DETALLE =
            "El enlace puede haber caducado o no ser válido. Solicita uno nuevo desde recuperar contraseña.";

    private static final String MENSAJE_ERROR_GENERICO_TITULO = "No se pudo actualizar la contraseña";
    private static final String MENSAJE_ERROR_GENERICO_DETALLE =
            "Ocurrió un problema al guardar los cambios. Intenta de nuevo más tarde o solicita un nuevo enlace.";

    private final PasswordResetTokenService passwordResetTokenService;

    public ResetPasswordController(PasswordResetTokenService passwordResetTokenService) {
        this.passwordResetTokenService = passwordResetTokenService;
    }

    @GetMapping("/reset")
    public String mostrarFormularioReset(@RequestParam(value = "token", required = false) String token, Model model) {
        try {
            passwordResetTokenService.validarToken(token);
            model.addAttribute("token", token != null ? token.trim() : "");
            return "reset-password";
        } catch (IllegalArgumentException ignored) {
            model.addAttribute("error", MENSAJE_ERROR_TITULO);
            model.addAttribute("message", MENSAJE_ERROR_DETALLE);
            return "error";
        }
    }

    @PostMapping("/reset")
    public String procesarReset(
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            Model model) {

        if (!StringUtils.hasText(token)) {
            model.addAttribute("error", MENSAJE_ERROR_TITULO);
            model.addAttribute("message", MENSAJE_ERROR_DETALLE);
            return "error";
        }

        String tokenTrim = token.trim();

        if (!StringUtils.hasText(password) || password.length() < 6) {
            return formularioConErrorTokenValido(tokenTrim, model,
                    "La contraseña debe tener al menos 6 caracteres.");
        }

        if (!password.equals(confirmPassword != null ? confirmPassword : "")) {
            return formularioConErrorTokenValido(tokenTrim, model, "Las contraseñas no coinciden.");
        }

        try {
            passwordResetTokenService.completarCambioContrasena(tokenTrim, password);
            return "reset-success";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", MENSAJE_ERROR_TITULO);
            model.addAttribute("message", MENSAJE_ERROR_DETALLE);
            return "error";
        } catch (Exception ex) {
            log.error("Error inesperado al restablecer contraseña", ex);
            model.addAttribute("error", MENSAJE_ERROR_GENERICO_TITULO);
            model.addAttribute("message", MENSAJE_ERROR_GENERICO_DETALLE);
            return "error";
        }
    }

    /**
     * Vuelve a mostrar el formulario con un mensaje de validación sin revelar si el correo existe.
     * Revalida el token para evitar fijar una sesión con token inválido.
     */
    private String formularioConErrorTokenValido(String tokenTrim, Model model, String mensajeFormulario) {
        try {
            passwordResetTokenService.validarToken(tokenTrim);
            model.addAttribute("token", tokenTrim);
            model.addAttribute("errorForm", mensajeFormulario);
            return "reset-password";
        } catch (IllegalArgumentException ignored) {
            model.addAttribute("error", MENSAJE_ERROR_TITULO);
            model.addAttribute("message", MENSAJE_ERROR_DETALLE);
            return "error";
        }
    }
}
