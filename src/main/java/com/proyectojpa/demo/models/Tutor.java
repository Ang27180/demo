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
@Table(name = "tutor")
public class Tutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tutor")
    private Integer idTutor;

    @Column(name = "experiencia", length = 255)
    private String experiencia;

    @Column(name = "observaciones", length = 255)
    private String observaciones;

    @Column(name = "cantidad")
    private Integer cantidad; // Nueva cantidad solicitada para el tutor

    /**
     * FK hacia {@code persona.id_persona}. En MySQL la tabla {@code tutor} tiene también
     * {@code persona_id_persona} (nullable); la columna obligatoria es {@code id_persona}.
     */
    @ManyToOne
    @JoinColumn(name = "id_persona", referencedColumnName = "id_persona", nullable = false)
    private Persona persona;


    // Getters y Setters
    public Integer getIdTutor() {
        return idTutor;
    }

    public void setIdTutor(Integer idTutor) {
        this.idTutor = idTutor;
    }

    public String getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(String experiencia) {
        this.experiencia = experiencia;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
