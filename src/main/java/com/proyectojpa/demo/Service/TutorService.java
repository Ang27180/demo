package com.proyectojpa.demo.Service;
import com.proyectojpa.demo.dto.TutorDTO;
import com.proyectojpa.demo.models.Tutor;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.TutorRepository;
import com.proyectojpa.demo.repository.PersonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /** Todas las personas con rol tutor (3), ordenadas por nombre (selectores admin / cursos). */
    public List<Persona> listarPersonasRolTutor() {
        return PersonaRepository.findByRolIdOrderByNombreAsc(3);
    }

    /**
     * Obtiene el {@link Tutor} vinculado a la persona o crea uno mínimo si aún no existe
     * (persona dada de alta solo con rol 3 en {@code persona}).
     */
    @Transactional
    public Tutor obtenerOCrearTutorParaPersonaId(Integer idPersona) {
        Persona persona = PersonaRepository.findById(idPersona)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + idPersona));
        if (persona.getRolId() == null || persona.getRolId() != 3) {
            throw new IllegalArgumentException("La persona debe tener rol tutor (id_rol = 3)");
        }
        return tutorRepository.findByPersona(persona).orElseGet(() -> {
            Tutor t = new Tutor();
            t.setPersona(persona);
            return tutorRepository.save(t);
        });
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
