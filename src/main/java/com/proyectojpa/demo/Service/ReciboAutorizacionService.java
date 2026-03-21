package com.proyectojpa.demo.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.models.Recibo;

@Service
public class ReciboAutorizacionService {

    @Transactional(readOnly = true)
    public boolean puedeVerRecibo(Persona persona, Recibo recibo) {
        if (persona == null || recibo == null || recibo.getInscripcion() == null
                || recibo.getInscripcion().getEstudiante() == null
                || recibo.getInscripcion().getEstudiante().getPersona() == null) {
            return false;
        }
        if (persona.getRolId() != null && persona.getRolId() == 1) {
            return true;
        }
        return persona.getId() != null && persona.getId()
                .equals(recibo.getInscripcion().getEstudiante().getPersona().getId());
    }
}
