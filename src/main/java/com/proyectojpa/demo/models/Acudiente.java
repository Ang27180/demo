package com.proyectojpa.demo.models;

import jakarta.persistence.*;

/**
 * Vincula la {@link Persona} del acudiente con el {@link Estudiante} dependiente.
 * {@link #parentesco} almacena MADRE, PADRE, OTRO, etc.
 */
@Entity
@Table(name = "acudiente")
public class Acudiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_acudiente")
    private Integer idAcudiente;

    /**
     * Columna física alineada con el esquema real (registro TI y consultas SQL):
     * {@code acudiente.id_persona} → {@code persona.id_persona}.
     */
    @ManyToOne
    @JoinColumn(name = "id_persona", referencedColumnName = "id_persona")
    private Persona persona;

    /**
     * {@code acudiente.id_estudiante_dependiente} → {@code estudiante.id_estudiante}.
     */
    @ManyToOne
    @JoinColumn(name = "id_estudiante_dependiente", referencedColumnName = "id_estudiante")
    private Estudiante estudianteDependiente;

    /** En BD histórica la columna se llama {@code Autorizacion} (datos del tutor en registro TI). */
    @Column(name = "Autorizacion", length = 45)
    private String parentesco;

    // Getters y Setters
    public Integer getIdAcudiente() {
        return idAcudiente;
    }

    public void setIdAcudiente(Integer idAcudiente) {
        this.idAcudiente = idAcudiente;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public Estudiante getEstudianteDependiente() {
        return estudianteDependiente;
    }

    public void setEstudianteDependiente(Estudiante estudianteDependiente) {
        this.estudianteDependiente = estudianteDependiente;
    }

    public String getParentesco() {
        return parentesco;
    }

    public void setParentesco(String parentesco) {
        this.parentesco = parentesco;
    }
}
