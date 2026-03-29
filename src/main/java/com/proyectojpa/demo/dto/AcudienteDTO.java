package com.proyectojpa.demo.dto;

/**
 * Data Transfer Object (DTO) implementado para manejar datos de Acudiente
 * previniendo envíos directos de los objetos persistidos, mediante JPA.
 */
public class AcudienteDTO {
    private Integer idAcudiente;
    private String autorizacion;
    private Integer idPersona;
    private Integer idEstudianteDependiente;

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

    public Integer getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(Integer idPersona) {
        this.idPersona = idPersona;
    }

    public Integer getIdEstudianteDependiente() {
        return idEstudianteDependiente;
    }

    public void setIdEstudianteDependiente(Integer idEstudianteDependiente) {
        this.idEstudianteDependiente = idEstudianteDependiente;
    }
}
