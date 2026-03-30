package com.proyectojpa.demo.dto;

public class AcudienteDTO {
    private Integer idAcudiente;
    private Integer idPersona;
    private Integer idEstudianteDependiente;
    /** MADRE, PADRE, OTRO, etc. */
    private String parentesco;

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

    public String getParentesco() {
        return parentesco;
    }

    public void setParentesco(String parentesco) {
        this.parentesco = parentesco;
    }
}
