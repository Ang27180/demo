package com.poryectojpa.demo.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "inscripcion")
public class Inscripcion {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "id_inscripcion")  // ← ESTE NOMBRE DEBE COINCIDIR
private Integer id;


    @Column(name = "Fecha_Inscripcion", nullable = false)
    private LocalDate fechaInscripcion;

    // Si quieres guardar solo el id del estudiante:
    @Column(name = "id_estudiante")
    private Integer idEstudiante;

    // Relación con Curso (usa la FK id_curso)
    @ManyToOne
    @JoinColumn(name = "id_curso")
    private Curso curso;

    // Relación con EstadoInscripcion (usa la FK id_estado)
    @ManyToOne
    @JoinColumn(name = "id_estado")
    private EstadoInscripcion estado;

    // Getters y setters

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

    public Integer getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(Integer idEstudiante) {
        this.idEstudiante = idEstudiante;
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
}
