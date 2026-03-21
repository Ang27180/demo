package com.poryectojpa.demo.dto;

public class MisCursosDTO {

    private Integer idCurso;
    private String nombreCurso;
    private String detalle;
    private String fechaInscripcion;
    private String estado;

    public MisCursosDTO(Integer idCurso, String nombreCurso, String detalle, String fechaInscripcion, String estado) {
        this.idCurso = idCurso;
        this.nombreCurso = nombreCurso;
        this.detalle = detalle;
        this.fechaInscripcion = fechaInscripcion;
        this.estado = estado;
    }

    public Integer getIdCurso() { return idCurso; }
    public String getNombreCurso() { return nombreCurso; }
    public String getDetalle() { return detalle; }
    public String getFechaInscripcion() { return fechaInscripcion; }
    public String getEstado() { return estado; }
}
