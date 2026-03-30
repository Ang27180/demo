package com.proyectojpa.demo.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "comprobante_pago")
public class ComprobantePago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comprobante")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_orden_pago", nullable = false)
    private OrdenPago ordenPago;

    @Column(name = "nombre_archivo_original", nullable = false, length = 255)
    private String nombreArchivoOriginal;

    /** Ruta relativa bajo {@code app.upload.dir} (ej. comprobantes/2026/03/uuid.pdf). */
    @Column(name = "ruta_almacenamiento", nullable = false, length = 512)
    private String rutaAlmacenamiento;

    @Column(name = "content_type", length = 128)
    private String contentType;

    @Column(name = "tamano_bytes")
    private Long tamanoBytes;

    @Column(name = "hash_sha256", length = 64)
    private String hashSha256;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida;

    /** Fecha/hora que el estudiante declara del pago en Nequi. */
    @Column(name = "fecha_hora_pago_reportada")
    private LocalDateTime fechaHoraPagoReportada;

    @Column(name = "telefono_pagador", length = 32)
    private String telefonoPagador;

    @Column(name = "ultimos_4_digitos", length = 8)
    private String ultimos4Digitos;

    @Column(name = "valor_reportado", precision = 12, scale = 2)
    private BigDecimal valorReportado;

    @Column(name = "observacion_estudiante", length = 1024)
    private String observacionEstudiante;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public OrdenPago getOrdenPago() {
        return ordenPago;
    }

    public void setOrdenPago(OrdenPago ordenPago) {
        this.ordenPago = ordenPago;
    }

    public String getNombreArchivoOriginal() {
        return nombreArchivoOriginal;
    }

    public void setNombreArchivoOriginal(String nombreArchivoOriginal) {
        this.nombreArchivoOriginal = nombreArchivoOriginal;
    }

    public String getRutaAlmacenamiento() {
        return rutaAlmacenamiento;
    }

    public void setRutaAlmacenamiento(String rutaAlmacenamiento) {
        this.rutaAlmacenamiento = rutaAlmacenamiento;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getTamanoBytes() {
        return tamanoBytes;
    }

    public void setTamanoBytes(Long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }

    public String getHashSha256() {
        return hashSha256;
    }

    public void setHashSha256(String hashSha256) {
        this.hashSha256 = hashSha256;
    }

    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(LocalDateTime fechaSubida) {
        this.fechaSubida = fechaSubida;
    }

    public LocalDateTime getFechaHoraPagoReportada() {
        return fechaHoraPagoReportada;
    }

    public void setFechaHoraPagoReportada(LocalDateTime fechaHoraPagoReportada) {
        this.fechaHoraPagoReportada = fechaHoraPagoReportada;
    }

    public String getTelefonoPagador() {
        return telefonoPagador;
    }

    public void setTelefonoPagador(String telefonoPagador) {
        this.telefonoPagador = telefonoPagador;
    }

    public String getUltimos4Digitos() {
        return ultimos4Digitos;
    }

    public void setUltimos4Digitos(String ultimos4Digitos) {
        this.ultimos4Digitos = ultimos4Digitos;
    }

    public BigDecimal getValorReportado() {
        return valorReportado;
    }

    public void setValorReportado(BigDecimal valorReportado) {
        this.valorReportado = valorReportado;
    }

    public String getObservacionEstudiante() {
        return observacionEstudiante;
    }

    public void setObservacionEstudiante(String observacionEstudiante) {
        this.observacionEstudiante = observacionEstudiante;
    }
}
