package com.proyectojpa.demo.models;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "recibo")
public class Recibo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recibo")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_inscripcion", nullable = false)
    private Inscripcion inscripcion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_medio_pago", nullable = false)
    private MedioPago medioPago;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    /** PENDIENTE o PAGADO */
    @Column(nullable = false, length = 16)
    private String estado;

    @Column(name = "codigo_qr_unico", nullable = false, unique = true, length = 96)
    private String codigoQrUnico;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Inscripcion getInscripcion() {
        return inscripcion;
    }

    public void setInscripcion(Inscripcion inscripcion) {
        this.inscripcion = inscripcion;
    }

    public MedioPago getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(MedioPago medioPago) {
        this.medioPago = medioPago;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCodigoQrUnico() {
        return codigoQrUnico;
    }

    public void setCodigoQrUnico(String codigoQrUnico) {
        this.codigoQrUnico = codigoQrUnico;
    }
}
