package com.proyectojpa.demo.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.proyectojpa.demo.dto.EstudianteDTO;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.EstudianteRepository;
import com.proyectojpa.demo.repository.PersonaRepository;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final PersonaRepository PersonaRepository;

    public EstudianteService(EstudianteRepository estudianteRepository,
                             PersonaRepository PersonaRepository) {
        this.estudianteRepository = estudianteRepository;
        this.PersonaRepository = PersonaRepository;
    }

  // =============================================
    // LISTAR ESTUDIANTES (DTO)
    // =============================================
    public List<EstudianteDTO> listar() {
        return estudianteRepository.findAll().stream().map(e -> {
            EstudianteDTO dto = new EstudianteDTO();
            dto.setIdEstudiante(e.getIdEstudiante());
            //dto.setContraseña(e.getContraseña());
            dto.setProgreso(e.getProgreso());
            dto.setIdEstadoEstudiante(e.getEstadoEstudiante());
            dto.setIdPersona(e.getPersona() != null ? e.getPersona().getId() : null);
            dto.setTutorNombre(e.getTutorNombre());
            dto.setTutorTelefono(e.getTutorTelefono());
            dto.setTutorEmail(e.getTutorEmail());
            return dto;
        }).collect(Collectors.toList());
    }

    // =============================================
    // GUARDAR (DTO)
    // =============================================
    public EstudianteDTO guardar(EstudianteDTO dto) {
        Estudiante e = new Estudiante();
        //e.setContraseña(dto.getContraseña());
        e.setProgreso(dto.getProgreso());
        e.setEstadoEstudiante(dto.getIdEstadoEstudiante());
        e.setTutorNombre(dto.getTutorNombre());
        e.setTutorTelefono(dto.getTutorTelefono());
        e.setTutorEmail(dto.getTutorEmail());

        Persona p = PersonaRepository.findById(dto.getIdPersona())
                .orElseThrow(() -> new RuntimeException("La persona no existe"));

        e.setPersona(p);

        estudianteRepository.save(e);

        dto.setIdEstudiante(e.getIdEstudiante());
        return dto;
    }

    // =============================================
    // ELIMINAR (DTO)
    // =============================================
    public void eliminar(Integer id) {
        estudianteRepository.deleteById(id);
    }

    // =============================================
    // 🔥 MÉTODOS QUE ESTABAN SIN IMPLEMENTAR
    // =============================================

    public List<Estudiante> findAll() {
        return estudianteRepository.findAll();
    }

    public void save(Estudiante e) {
        estudianteRepository.save(e);
    }

    public void delete(Integer id) {
        estudianteRepository.deleteById(id);
    }

    public java.util.Optional<Estudiante> findByPersona(Persona p) {
        return estudianteRepository.findByPersona(p);
    }
}

