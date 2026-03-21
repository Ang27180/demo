package com.poryectojpa.demo.models;

import jakarta.persistence.*;

@Entity
@Table(name = "acudiente")
public class Acudiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAcudiente")
    private Integer idAcudiente;

    @Column(name = "Autorizacion", length = 45)
    private String autorizacion;

    @ManyToOne
    @JoinColumn(name = "Persona_idPersona", referencedColumnName = "id_persona")
    private Persona persona;

    @ManyToOne
    @JoinColumn(name = "Id_Estudiente_dependiente", referencedColumnName = "id_estudiante")
    private Estudiante estudianteDependiente;

    // Getters y Setters
    public Integer getIdAcudiente() {
        return idAcudiente;
    }

    public void setIdAcudiente(Integer idAcudiente) {
        this.idAcudiente = idAcudiente;
    }

    public String getAutorizacion() {
        return autorizacion;
    }

    public void setAutorizacion(String autorizacion) {
        this.autorizacion = autorizacion;
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
