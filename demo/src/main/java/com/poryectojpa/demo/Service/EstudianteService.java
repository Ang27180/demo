package com.poryectojpa.demo.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.poryectojpa.demo.dto.EstudianteDTO;
import com.poryectojpa.demo.models.Estudiante;
import com.poryectojpa.demo.models.Persona;
import com.poryectojpa.demo.repository.EstudianteRepository;
import com.poryectojpa.demo.repository.personaRepository;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final personaRepository personaRepository;

    public EstudianteService(EstudianteRepository estudianteRepository,
                             personaRepository personaRepository) {
        this.estudianteRepository = estudianteRepository;
        this.personaRepository = personaRepository;
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
            dto.setIdPersona(e.getPersona().getId());
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

        Persona p = personaRepository.findById(dto.getIdPersona())
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
}