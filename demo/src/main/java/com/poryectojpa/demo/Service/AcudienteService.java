package com.poryectojpa.demo.Service;

import com.poryectojpa.demo.dto.AcudienteDTO;
import com.poryectojpa.demo.models.Acudiente;
import com.poryectojpa.demo.models.Persona;
import com.poryectojpa.demo.models.Estudiante;
import com.poryectojpa.demo.repository.AcudienteRepository;
import com.poryectojpa.demo.repository.personaRepository;
import com.poryectojpa.demo.repository.EstudianteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AcudienteService {

    private final AcudienteRepository acudienteRepository;
    private final personaRepository personaRepository;
    private final EstudianteRepository estudianteRepository;

    public AcudienteService(AcudienteRepository acudienteRepository,
                            personaRepository personaRepository,
                            EstudianteRepository estudianteRepository) {
        this.acudienteRepository = acudienteRepository;
        this.personaRepository = personaRepository;
        this.estudianteRepository = estudianteRepository;
    }

    private AcudienteDTO toDTO(Acudiente entity) {
        AcudienteDTO dto = new AcudienteDTO();
        dto.setIdAcudiente(entity.getIdAcudiente());
        dto.setAutorizacion(entity.getAutorizacion());
        dto.setIdPersona(entity.getPersona().getId());
        dto.setIdEstudianteDependiente(
                entity.getEstudianteDependiente() != null ?
                        entity.getEstudianteDependiente().getIdEstudiante() : null
        );
        return dto;
    }

    private Acudiente toEntity(AcudienteDTO dto) {
        Acudiente acudiente = new Acudiente();
        acudiente.setIdAcudiente(dto.getIdAcudiente());
        acudiente.setAutorizacion(dto.getAutorizacion());

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

    public List<AcudienteDTO> listar() {
        return acudienteRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AcudienteDTO guardar(AcudienteDTO dto) {
        Acudiente entidad = toEntity(dto);
        Acudiente guardado = acudienteRepository.save(entidad);
        return toDTO(guardado);
    }

    public void eliminar(Integer id) {
        acudienteRepository.deleteById(id);
    }
}
