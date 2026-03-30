package com.proyectojpa.demo.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "leccion")
public class Leccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_leccion")
    private Integer id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "contenido_tipo") // video, texto, pdf
    private String contenidoTipo;

    @Column(name = "contenido_url", length = 2048)
    private String contenidoUrl;

    /** Texto largo para lecciones tipo lectura (HTML escapado en vista). */
    @Lob
    @Column(name = "contenido_texto", columnDefinition = "LONGTEXT")
    private String contenidoTexto;

    @ManyToOne
    @JoinColumn(name = "id_modulo", nullable = false)
    private Modulo modulo;

    @Column(name = "cantidad")
    private Integer cantidad; // Nueva cantidad solicitada

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContenidoTipo() {
        return contenidoTipo;
    }

    public void setContenidoTipo(String contenidoTipo) {
        this.contenidoTipo = contenidoTipo;
    }

    public String getContenidoUrl() {
        return contenidoUrl;
    }

    public void setContenidoUrl(String contenidoUrl) {
        this.contenidoUrl = contenidoUrl;
    }

    public String getContenidoTexto() {
        return contenidoTexto;
    }

    public void setContenidoTexto(String contenidoTexto) {
        this.contenidoTexto = contenidoTexto;
    }

    public Modulo getModulo() {
        return modulo;
    }

    public void setModulo(Modulo modulo) {
        this.modulo = modulo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
