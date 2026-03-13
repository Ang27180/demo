package com.proyectojpa.demo.dto;

public class EstudianteDTO {

    private Integer idEstudiante;
    private String contraseña;
    private String progreso;
    private Integer idEstadoEstudiante;
    private String tutorNombre;
    private String tutorTelefono;
    private String tutorEmail;
    private Integer idPersona;

    // Getters & Setters
    public Integer getIdEstudiante() { return idEstudiante; }
    public void setIdEstudiante(Integer idEstudiante) { this.idEstudiante = idEstudiante; }

    public String getContraseña() { return contraseña; }
    public void setContraseña(String contraseña) { this.contraseña = contraseña; }

    public String getProgreso() { return progreso; }
    public void setProgreso(String progreso) { this.progreso = progreso; }

    public Integer getIdEstadoEstudiante() { return idEstadoEstudiante; }
    public void setIdEstadoEstudiante(Integer idEstadoEstudiante) { this.idEstadoEstudiante = idEstadoEstudiante; }

    public Integer getIdPersona() { return idPersona; }
    public void setIdPersona(Integer idPersona) { this.idPersona = idPersona; }

    public String getTutorNombre() { return tutorNombre; }
    public void setTutorNombre(String tutorNombre) { this.tutorNombre = tutorNombre; }

    public String getTutorTelefono() { return tutorTelefono; }
    public void setTutorTelefono(String tutorTelefono) { this.tutorTelefono = tutorTelefono; }

    public String getTutorEmail() { return tutorEmail; }
    public void setTutorEmail(String tutorEmail) { this.tutorEmail = tutorEmail; }
}

