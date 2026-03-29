package com.proyectojpa.demo.models;

import jakarta.persistence.*;

/**
 * Entidad Acudiente: Mapeada para encajar con la base de datos legacy 'sabormasterclass'.
 * Se han ajustado los nombres de las columnas para que coincidan con el Dump SQL.
 */
@Entity
@Table(name = "acudiente")
public class Acudiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_acudiente") // Corregido según DB: id_acudiente
    private Integer idAcudiente;

    @Column(name = "Autorizacion", length = 45) // Ajustado a CamelCase según DB
    private String autorizacion;

    /**
     * Relación con Persona: La tabla real en DB usa 'persona_id_persona'.
     * Existe otra columna duplicada 'Persona_idPersona' que está mayormente vacía en el dump.
     */
    @ManyToOne
    @JoinColumn(name = "persona_id_persona", referencedColumnName = "id_persona")
    private Persona persona;

    /**
     * Relación con Estudiante: La tabla tiene un error tipográfico en DB 'Id_Estudiente_dependiente'
     * (con 'e' en Estudiente). Lo respetaremos para que la consulta funcione sin errores.
     */
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
