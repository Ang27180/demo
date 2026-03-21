package com.proyectojpa.demo.models;

import jakarta.persistence.*;

@Entity
@Table(name = "estado_inscripcion")
public class EstadoInscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * Clave estable: ACTIVO, PENDIENTE_PAGO, etc.
     */
    @Column(name = "codigo", unique = true, length = 32, nullable = true)
    private String codigo;

    private String nombre;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
