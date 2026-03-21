package com.proyectojpa.demo.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "medio_pago")
public class MedioPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_medio_pago")
    private Integer id;

    @Column(nullable = false, length = 120)
    private String nombre;

    /** Ej.: TRANSFERENCIA, QR */
    @Column(nullable = false, length = 32)
    private String tipo;

    /** Base64 de imagen QR del medio o ruta; TEXT */
    @Column(name = "imagen_qr", columnDefinition = "TEXT")
    private String imagenQr;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_persona_admin", referencedColumnName = "id_persona", nullable = false)
    private Persona adminPersona;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getImagenQr() {
        return imagenQr;
    }

    public void setImagenQr(String imagenQr) {
        this.imagenQr = imagenQr;
    }

    public Persona getAdminPersona() {
        return adminPersona;
    }

    public void setAdminPersona(Persona adminPersona) {
        this.adminPersona = adminPersona;
    }
}
