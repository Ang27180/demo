package com.proyectojpa.demo.Service;

import java.io.File;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender,
            @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    @Override
    public void enviarTexto(String destinatario, String asunto, String mensaje) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(fromEmail);
        mail.setTo(destinatario);
        mail.setSubject(asunto);
        mail.setText(mensaje);
        mailSender.send(mail);
    }

    @Override
    public void enviarHtml(String para, String asunto, String html) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(para);
        helper.setSubject(asunto);
        helper.setText(html, true);
        mailSender.send(message);
    }

    @Override
    public void enviarConAdjunto(String para, String asunto, String mensaje, String rutaArchivo)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(para);
        helper.setSubject(asunto);
        helper.setText(mensaje);
        helper.addAttachment("archivo", new File(rutaArchivo));
        mailSender.send(message);
    }
}
