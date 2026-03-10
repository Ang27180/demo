package com.poryectojpa.demo.controller;

import com.poryectojpa.demo.Service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/correo")
public class CorreoController {

    @Autowired
    private EmailService emailService;

    // ============================================================
    // 1️⃣ VISTA THYMELEAF
    // ============================================================
    @GetMapping("/formulario")
    public String mostrarFormulario() {
        return "correoFormulario"; // ARCHIVO HTML QUE ME PASASTE
    }

    // ============================================================
    // 2️⃣ ENVÍO DESDE LA VISTA (MEJORADO)
    // ============================================================
    @PostMapping("/enviar-desde-vista")
    public String enviarDesdeVista(
            @RequestParam String para,
            @RequestParam String asunto,
            @RequestParam String mensaje,
            RedirectAttributes redirectAttributes) {

        try {
            // Se genera el contenido HTML con la nueva plantilla premium diseñada para el proyecto
            // Se eliminó el parámetro 'tipo' para evitar el Error 400 cuando viene de la vista
            String html = generarPlantillaPremium(para, asunto, mensaje);
            
            // Se envía forzosamente como HTML para mantener la estética institucional
            emailService.enviarHtml(para, asunto, html);

            redirectAttributes.addFlashAttribute("mensaje", "Correo enviado correctamente con la nueva plantilla premium");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");

        } catch (Exception e) {
            // Si falla el envío (por ejemplo, SMTP mal configurado), se captura y muestra en el formulario
            redirectAttributes.addFlashAttribute("mensaje", "Error al enviar el correo: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/correo/formulario";
    }

    // ============================================================
    // 3️⃣ ENDPOINTS PARA POSTMAN (API)
    // ============================================================
    @ResponseBody
    @PostMapping("/texto")
    public ResponseEntity<String> enviarTexto(
            @RequestParam String para,
            @RequestParam String asunto,
            @RequestParam String mensaje) {

        emailService.enviarTexto(para, asunto, mensaje);
        return ResponseEntity.ok("Correo enviado correctamente (texto plano).");
    }

    @ResponseBody
    @PostMapping("/html")
    public ResponseEntity<String> enviarHtml(
            @RequestParam String para,
            @RequestParam String asunto) throws MessagingException {

        String html = generarPlantillaPremium(para, asunto, "Mensaje de bienvenida automático");
        emailService.enviarHtml(para, asunto, html);

        return ResponseEntity.ok("Correo enviado correctamente (HTML).");
    }

    @ResponseBody
    @PostMapping("/adjunto")
    public ResponseEntity<String> enviarAdjunto(
            @RequestParam String para,
            @RequestParam String asunto,
            @RequestParam String mensaje,
            @RequestParam String archivo) throws MessagingException {

        emailService.enviarConAdjunto(para, asunto, mensaje, archivo);
        return ResponseEntity.ok("Correo enviado con adjunto.");
    }

    // ============================================================
    // 4️⃣ ENDPOINTS DE PRUEBA (GET)
    // ============================================================
    @ResponseBody
    @GetMapping("/test/texto")
    public String pruebaTexto() {
        try {
            emailService.enviarTexto(
                    "sabormasterclass@gmail.com",
                    "Prueba Texto",
                    "Este es un mensaje de prueba");
            return "✔ Correo (texto) enviado correctamente";
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    @ResponseBody
    @GetMapping("/test/html")
    public String pruebaHtml() {
        try {
            emailService.enviarHtml(
                    "TU_CORREO@gmail.com",
                    "Prueba HTML",
                    "<h1 style='color:blue;'>Correo HTML de prueba</h1>");
            return "✔ Correo (HTML) enviado correctamente";
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    @ResponseBody
    @GetMapping("/test/adjunto")
    public String pruebaAdjunto() {
        try {
            emailService.enviarConAdjunto(
                    "TU_CORREO@gmail.com",
                    "Prueba Adjunto",
                    "Este correo contiene un archivo adjunto.",
                    "C:/ruta/archivo.pdf");
            return "✔ Correo (adjunto) enviado correctamente";
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    /**
     * Genera una plantilla HTML premium con los colores institucionales (Negro, Dorado y Blanco)
     */
    private String generarPlantillaPremium(String para, String asunto, String mensaje) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body style="margin: 0; padding: 0; background-color: #f4f4f4; font-family: 'Segoe UI', Arial, sans-serif;">
                    <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 12px; overflow: hidden; margin-top: 30px; margin-bottom: 30px; box-shadow: 0 4px 15px rgba(0,0,0,0.1);">
                        
                        <!-- Cabecera Premium (Negro con acento Morado) -->
                        <tr>
                            <td align="center" style="background-color: #000000; padding: 40px 20px; border-bottom: 4px solid #6619f5;">
                                <h1 style="color: #ffffff; margin: 0; font-size: 28px; letter-spacing: 1px;">Sabor MasterClass</h1>
                            </td>
                        </tr>

                        <!-- Cuerpo del Mensaje -->
                        <tr>
                            <td style="padding: 40px 30px;">
                                <h2 style="color: #333333; margin-top: 0; font-size: 20px;">Asunto: %s</h2>
                                <p style="color: #555555; font-size: 16px; line-height: 1.6; margin-bottom: 25px;">
                                    Hola <b>%s</b>,
                                </p>
                                <div style="background-color: #f9f9f9; padding: 25px; border-radius: 8px; border-left: 5px solid #000000; color: #444444; font-size: 16px; line-height: 1.6;">
                                    %s
                                </div>
                                <p style="color: #555555; font-size: 16px; line-height: 1.6; margin-top: 25px;">
                                    Si tienes alguna pregunta, no dudes en ponerte en contacto con nuestro equipo de soporte.
                                </p>
                                
                                <!-- Botón de Acción -->
                                <table align="center" border="0" cellpadding="0" cellspacing="0" style="margin-top: 30px;">
                                    <tr>
                                        <td align="center" style="border-radius: 50px; background-color: #000000;">
                                            <a href="http://localhost:8080/home" style="display: inline-block; padding: 12px 35px; color: #ffffff; text-decoration: none; font-weight: bold; border-radius: 50px; font-size: 14px;">
                                                Ir a Sabor MasterClass
                                            </a>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>

                        <!-- Pie de página (Negro) -->
                        <tr>
                            <td align="center" style="background-color: #000000; padding: 30px 20px; color: #aaaaaa; font-size: 12px;">
                                <p style="margin: 0; color: #ffffff;">Sabor MasterClass © 2025</p>
                                <p style="margin: 5px 0 0 0;">Todos los derechos reservados. Desarrollado por HAMN</p>
                                <div style="margin-top: 15px;">
                                    <span style="color: #6619f5;">•</span> Facebook <span style="color: #6619f5;">•</span> Instagram <span style="color: #6619f5;">•</span> Twitter
                                </div>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """
                .formatted(asunto, para, mensaje);
    }
}
