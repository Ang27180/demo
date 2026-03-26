package com.poryectojpa.demo.Service;
import com.poryectojpa.demo.dto.TutorDTO;
import com.poryectojpa.demo.models.Tutor;
import com.poryectojpa.demo.models.Persona;
import com.poryectojpa.demo.repository.TutorRepository;
import com.poryectojpa.demo.repository.personaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TutorService {

    private final TutorRepository tutorRepository;
    private final personaRepository personaRepository;

    public TutorService(TutorRepository tutorRepository, personaRepository personaRepository) {
        this.tutorRepository = tutorRepository;
        this.personaRepository = personaRepository;
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
            Persona persona = personaRepository.findById(dto.getIdPersona()).orElse(null);
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
