package com.proyectojpa.demo.models;

import java.time.LocalDate;
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
@Table(name = "inscripcion")
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscripcion")
    private Integer id;

    @Column(name = "Fecha_Inscripcion", nullable = false)
    private LocalDate fechaInscripcion;

    @ManyToOne
    @JoinColumn(name = "id_estudiante", nullable = false)
    private Estudiante estudiante;

    @ManyToOne
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    @ManyToOne
    @JoinColumn(name = "id_estado", nullable = false)
    private EstadoInscripcion estado;

    /** Plazo para completar el pago (inscripciones PENDIENTE_PAGO). */
    @Column(name = "fecha_limite_pago")
    private LocalDate fechaLimitePago;

    /**
     * Autorización expresa del acudiente/tutor para emitir el certificado final.
     * Mapeado a la base de datos para funcionar con la vista de Acudiente.
     */
    @Column(name = "certificado_autorizado")
    private Boolean certificadoAutorizado = false;

    /** Primera solicitud de recibo de pago (estudiante con acudiente) — notificación al acudiente. */
    @Column(name = "fecha_solicitud_recibo_acudiente")
    private LocalDateTime fechaSolicitudReciboAcudiente;

    /** Solicitud de autorización de certificado (estudiante con acudiente). */
    @Column(name = "fecha_solicitud_certificado_acudiente")
    private LocalDateTime fechaSolicitudCertificadoAcudiente;

    /** Cuando el acudiente autoriza la descarga del certificado. */
    @Column(name = "fecha_autorizacion_certificado")
    private LocalDateTime fechaAutorizacionCertificado;

    // getters y setters

    public Boolean getCertificadoAutorizado() {
        return certificadoAutorizado;
    }

    public void setCertificadoAutorizado(Boolean certificadoAutorizado) {
        this.certificadoAutorizado = certificadoAutorizado;
    }

    public LocalDateTime getFechaSolicitudReciboAcudiente() {
        return fechaSolicitudReciboAcudiente;
    }

    public void setFechaSolicitudReciboAcudiente(LocalDateTime fechaSolicitudReciboAcudiente) {
        this.fechaSolicitudReciboAcudiente = fechaSolicitudReciboAcudiente;
    }

    public LocalDateTime getFechaSolicitudCertificadoAcudiente() {
        return fechaSolicitudCertificadoAcudiente;
    }

    public void setFechaSolicitudCertificadoAcudiente(LocalDateTime fechaSolicitudCertificadoAcudiente) {
        this.fechaSolicitudCertificadoAcudiente = fechaSolicitudCertificadoAcudiente;
    }

    public LocalDateTime getFechaAutorizacionCertificado() {
        return fechaAutorizacionCertificado;
    }

    public void setFechaAutorizacionCertificado(LocalDateTime fechaAutorizacionCertificado) {
        this.fechaAutorizacionCertificado = fechaAutorizacionCertificado;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDate fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public EstadoInscripcion getEstado() {
        return estado;
    }

    public void setEstado(EstadoInscripcion estado) {
        this.estado = estado;
    }

    public LocalDate getFechaLimitePago() {
        return fechaLimitePago;
    }

    public void setFechaLimitePago(LocalDate fechaLimitePago) {
        this.fechaLimitePago = fechaLimitePago;
    }
}
