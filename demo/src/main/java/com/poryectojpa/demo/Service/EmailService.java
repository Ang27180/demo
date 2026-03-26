package com.poryectojpa.demo.Service;

import jakarta.mail.MessagingException;

public interface EmailService {

    void enviarTexto(String para, String asunto, String mensaje);

    void enviarHtml(String para, String asunto, String html) throws MessagingException;

    void enviarConAdjunto(String para, String asunto, String mensaje, String rutaArchivo) throws MessagingException;

}

    

