package com.proyectojpa.demo.dto;

public class AcudienteDTO {
    private Integer idAcudiente;
    private Integer idPersona;
    private Integer idEstudianteDependiente;

    public Integer getIdAcudiente() {
        return idAcudiente;
    }

    public void setIdAcudiente(Integer idAcudiente) {
        this.idAcudiente = idAcudiente;
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
