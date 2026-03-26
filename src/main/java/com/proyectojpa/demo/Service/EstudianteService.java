package com.proyectojpa.demo.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.proyectojpa.demo.dto.EstudianteDTO;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.EstadoInscripcionRepository;
import com.proyectojpa.demo.repository.EstudianteRepository;
import com.proyectojpa.demo.repository.PersonaRepository;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final PersonaRepository PersonaRepository;
    private final EstadoInscripcionRepository estadoInscripcionRepository;

    public EstudianteService(EstudianteRepository estudianteRepository,
                             PersonaRepository PersonaRepository,
                             EstadoInscripcionRepository estadoInscripcionRepository) {
        this.estudianteRepository = estudianteRepository;
        this.PersonaRepository = PersonaRepository;
        this.estadoInscripcionRepository = estadoInscripcionRepository;
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
            dto.setIdEstadoEstudiante(e.getEstadoEstudiante() != null ? e.getEstadoEstudiante().getId() : null);
            dto.setIdPersona(e.getPersona() != null ? e.getPersona().getId() : null);
            if (e.getPersona() != null) {
                dto.setTutorNombre(e.getPersona().getTutorNombre());
                dto.setTutorTelefono(e.getPersona().getTutorTelefono());
                dto.setTutorEmail(e.getPersona().getTutorEmail());
            }
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
        if (dto.getIdEstadoEstudiante() != null) {
            estadoInscripcionRepository.findById(dto.getIdEstadoEstudiante()).ifPresent(e::setEstadoEstudiante);
        }
        Persona p = PersonaRepository.findById(dto.getIdPersona())
                .orElseThrow(() -> new RuntimeException("La persona no existe"));

        if (dto.getTutorNombre() != null) {
            p.setTutorNombre(dto.getTutorNombre());
        }
        if (dto.getTutorTelefono() != null) {
            p.setTutorTelefono(dto.getTutorTelefono());
        }
        if (dto.getTutorEmail() != null) {
            p.setTutorEmail(dto.getTutorEmail());
        }
        PersonaRepository.save(p);

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

