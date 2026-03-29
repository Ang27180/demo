package com.proyectojpa.demo.Service;

import com.proyectojpa.demo.dto.AcudienteDTO;
import com.proyectojpa.demo.models.Acudiente;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.repository.AcudienteRepository;
import com.proyectojpa.demo.repository.PersonaRepository; // Corregido el import
import com.proyectojpa.demo.repository.EstudianteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio AcudienteService maneja lógica de negocio para crear, listar y eliminar acudientes
 * transformando DTOs hacia Entidades antes de guardarlos en repositorio.
 */
@Service
public class AcudienteService {

    private final AcudienteRepository acudienteRepository;
    private final PersonaRepository personaRepository;
    private final EstudianteRepository estudianteRepository;

    public AcudienteService(AcudienteRepository acudienteRepository,
                            PersonaRepository personaRepository,
                            EstudianteRepository estudianteRepository) {
        this.acudienteRepository = acudienteRepository;
        this.personaRepository = personaRepository;
        this.estudianteRepository = estudianteRepository;
    }

    // Convertidor de Entidad a un DTO para presentarlo como respuesta
    private AcudienteDTO toDTO(Acudiente entity) {
        AcudienteDTO dto = new AcudienteDTO();
        dto.setIdAcudiente(entity.getIdAcudiente());
        dto.setIdPersona(entity.getPersona() != null ? entity.getPersona().getId() : null);
        dto.setIdEstudianteDependiente(
                entity.getEstudianteDependiente() != null ?
                        entity.getEstudianteDependiente().getIdEstudiante() : null
        );
        return dto;
    }

    // Convertidor de DTO a Entidad buscando información adicional como la Persona y el Estudiante
    private Acudiente toEntity(AcudienteDTO dto) {
        Acudiente acudiente = new Acudiente();
        acudiente.setIdAcudiente(dto.getIdAcudiente());

        Persona persona = personaRepository.findById(dto.getIdPersona())
                .orElseThrow(() -> new RuntimeException("Persona no encontrada"));

        Estudiante estudiante = null;
        if (dto.getIdEstudianteDependiente() != null) {
            estudiante = estudianteRepository.findById(dto.getIdEstudianteDependiente())
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        }

        acudiente.setPersona(persona);
        acudiente.setEstudianteDependiente(estudiante);

        return acudiente;
    }

    // Obtener todos los Acudientes
    public List<AcudienteDTO> listar() {
        return acudienteRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Insertar un Acudiente
    public AcudienteDTO guardar(AcudienteDTO dto) {
        Acudiente entidad = toEntity(dto);
        Acudiente guardado = acudienteRepository.save(entidad);
        return toDTO(guardado);
    }

    // Borrado físico de un Acudiente
    public void eliminar(Integer id) {
        acudienteRepository.deleteById(id);
    }
}
