package com.proyectojpa.demo.models;

import jakarta.persistence.*;

/**
 * Entidad Acudiente: Versión DEFINITIVA con solo 3 columnas.
 * id_acudiente, Id_persona e Id_Estudiante_dependiente.
 */
@Entity
@Table(name = "acudiente")
public class Acudiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_acudiente")
    private Integer idAcudiente;

    @ManyToOne
    @JoinColumn(name = "Id_persona", referencedColumnName = "id_persona")
    private Persona persona;

    @ManyToOne
    @JoinColumn(name = "Id_Estudiante_dependiente", referencedColumnName = "id_estudiante")
    private Estudiante estudianteDependiente;

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
}
