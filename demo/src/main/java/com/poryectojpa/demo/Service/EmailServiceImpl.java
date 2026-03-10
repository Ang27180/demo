package com.poryectojpa.demo.Service;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.poryectojpa.demo.Service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Se inyecta el correo configurado en application.properties para evitar errores de remitente desconocido
    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void enviarTexto(String para, String asunto, String mensaje) {
        SimpleMailMessage mail = new SimpleMailMessage();
        // Se establece explícitamente el remitente para corregir el error "can't determine local email address"
        mail.setFrom(fromEmail);
        mail.setTo(para);
        mail.setSubject(asunto);
        mail.setText(mensaje);

        mailSender.send(mail);
    }

    @Override
    public void enviarHtml(String para, String asunto, String html) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // Uso del remitente configurado para envíos HTML
        helper.setFrom(fromEmail);
        helper.setTo(para);
        helper.setSubject(asunto);
        helper.setText(html, true);

        mailSender.send(message);
    }

    @Override
    public void enviarConAdjunto(String para, String asunto, String mensaje, String rutaArchivo) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // Uso del remitente configurado para envíos con adjuntos
        helper.setFrom(fromEmail);
        helper.setTo(para);
        helper.setSubject(asunto);
        helper.setText(mensaje);

        helper.addAttachment("archivo", new java.io.File(rutaArchivo));

        mailSender.send(message);
    }
}
