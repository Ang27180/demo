package com.proyectojpa.demo.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

@Service
public class QrCodeService {

    private static final int ANCHO = 220;

    /**
     * PNG en Base64 (sin prefijo data:) para incrustar en HTML o reportes.
     */
    public String generarPngBase64(String texto) {
        return Base64.getEncoder().encodeToString(generarPngBytes(texto));
    }

    /** Bytes PNG para Jasper u otros usos binarios. */
    public byte[] generarPngBytes(String texto) {
        try {
            QRCodeWriter qr = new QRCodeWriter();
            BitMatrix matrix = qr.encode(texto, BarcodeFormat.QR_CODE, ANCHO, ANCHO);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el código QR", e);
        }
    }
}
