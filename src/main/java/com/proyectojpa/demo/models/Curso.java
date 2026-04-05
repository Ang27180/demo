package com.proyectojpa.demo.models;

import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "curso")

public class Curso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_curso")
    private Integer id;

    @Column(name = "duracion")
    private String duracion;

    @Column(name = "numero_curso")
    private String numcurso;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "Detalle")
    private String detalle;

    @Column(name = "costo")
    private Double costo;

    @Column(name = "nivel_aprendizaje")
    private String aprendizaje;

    @Column(name = "categoria")
    private Integer categoria;

    @Column(name = "imagen")
    private String imagen;

    @jakarta.persistence.ManyToOne
    @jakarta.persistence.JoinColumn(name = "id_tutor")
    private Tutor tutor;

    @jakarta.persistence.OneToMany(mappedBy = "curso")
    @OrderBy("id ASC")
    private Set<Modulo> modulos;

    public Set<Modulo> getModulos() {
        return modulos;
    }

    public void setModulos(Set<Modulo> modulos) {
        this.modulos = modulos;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public String getNumcurso() {
        return numcurso;
    }

    public void setNumcurso(String numcurso) {
        this.numcurso = numcurso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public Double getCosto() {
        return costo;
    }

    public void setCosto(Double costo) {
        this.costo = costo;
    }

    public String getAprendizaje() {
        return aprendizaje;
    }

    public void setAprendizaje(String aprendizaje) {
        this.aprendizaje = aprendizaje;
    }

    public Integer getCategoria() {
        return categoria;
    }

    public void setCategoria(Integer categoria) {
        this.categoria = categoria;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Tutor getTutor() {
        return tutor;
    }

    public void setTutor(Tutor tutor) {
        this.tutor = tutor;
    }
}
