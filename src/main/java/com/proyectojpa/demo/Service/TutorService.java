package com.proyectojpa.demo.Service;
import com.proyectojpa.demo.dto.TutorDTO;
import com.proyectojpa.demo.models.Tutor;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.TutorRepository;
import com.proyectojpa.demo.repository.PersonaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TutorService {

    private final TutorRepository tutorRepository;
    private final PersonaRepository PersonaRepository;

    public TutorService(TutorRepository tutorRepository, PersonaRepository PersonaRepository) {
        this.tutorRepository = tutorRepository;
        this.PersonaRepository = PersonaRepository;
    }

    public List<TutorDTO> listar() {
        return tutorRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TutorDTO guardar(TutorDTO dto) {

        Tutor tutor = new Tutor();
        tutor.setIdTutor(dto.getIdTutor());
        tutor.setExperiencia(dto.getExperiencia());
        tutor.setObservaciones(dto.getObservaciones());

        if (dto.getIdPersona() != null) {
            Persona persona = PersonaRepository.findById(dto.getIdPersona()).orElse(null);
            tutor.setPersona(persona);
        }

        Tutor guardado = tutorRepository.save(tutor);
        return toDTO(guardado);
    }

    private TutorDTO toDTO(Tutor t) {
        TutorDTO dto = new TutorDTO();
        dto.setIdTutor(t.getIdTutor());
        dto.setExperiencia(t.getExperiencia());
        dto.setObservaciones(t.getObservaciones());

        if (t.getPersona() != null) {
            dto.setIdPersona(t.getPersona().getId());
        }

        return dto;
    }

    public void eliminar(Integer id) {
        tutorRepository.deleteById(id);
    }
}
