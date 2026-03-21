package com.poryectojpa.demo.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "curso")

public class Curso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_curso")
    private Integer id;

    @Column(name = "Duracion")
    private String duracion;

    @Column(name = "Numero_Curso")
    private String numcurso;

    @Column(name = "Nombre")
    private String nombre;

    @Column(name = "Detalle")
    private String detalle;

    @Column(name = "Costo")
    private Double costo;

    @Column(name = "Nivel_Aprendizaje")
    private String aprendizaje;

    @Column(name = "Categoria")
    private Integer categoria;

    @Column(name = "imagen")
    private String imagen;

    @jakarta.persistence.OneToMany(mappedBy = "curso")
    private java.util.List<Modulo> modulos;

    public java.util.List<Modulo> getModulos() {
        return modulos;
    }

    public void setModulos(java.util.List<Modulo> modulos) {
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


    
}
