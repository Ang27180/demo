package com.proyectojpa.demo.models;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rol")
public class Rol implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id_Rol")
    private Integer id;

    /** Columna real en MySQL: descripcionRol (camelCase). No mapear también descripcion_rol: la naming strategy la fusionaba y Hibernate fallaba al arrancar. */
    @Column(name = "descripcionRol", length = 20)
    private String descripcionRol;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreVisible() {
        if (descripcionRol != null && !descripcionRol.isBlank()) {
            return descripcionRol.trim();
        }
        return "Rol " + id;
    }

    public String getDescripcionRol() {
        return descripcionRol;
    }

    public void setDescripcionRol(String descripcionRol) {
        this.descripcionRol = descripcionRol;
    }
}
