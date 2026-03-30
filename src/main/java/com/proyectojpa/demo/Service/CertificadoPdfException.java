package com.proyectojpa.demo.Service;

/**
 * Fallo al compilar o exportar el certificado Jasper a PDF (después de autorización).
 */
public class CertificadoPdfException extends Exception {

    private static final long serialVersionUID = 1L;

    public CertificadoPdfException(String message, Throwable cause) {
        super(message, cause);
    }
}
