package com.poryectojpa.demo.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "estudiante")
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estudiante")
    private Integer idEstudiante;

    @Column(name = "id_estado_estudiante")
    private Integer estadoEstudiante; // 

    @Column(name = "progreso", nullable = false, length = 45)
    private String progreso;

    @ManyToOne
    @JoinColumn(name = "persona_id_persona", referencedColumnName = "id_persona")
    private Persona persona;

    // Getters y Setters
    public Integer getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(Integer idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public Integer getEstadoEstudiante() {
        return estadoEstudiante;
    }

    public void setEstadoEstudiante(Integer estadoEstudiante) {
        this.estadoEstudiante = estadoEstudiante;
    }
    
    public String getProgreso() {
        return progreso;
    }

    public void setProgreso(String progreso) {
        this.progreso = progreso;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }
}
