package com.proyectojpa.demo.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.proyectojpa.demo.dto.TutorDTO;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.models.Tutor;
import com.proyectojpa.demo.repository.PersonaRepository;
import com.proyectojpa.demo.repository.TutorRepository;

@Service
public class TutorService {

    private final TutorRepository tutorRepository;
    private final PersonaRepository PersonaRepository;
    private final FileStorageService fileStorageService;

    public TutorService(TutorRepository tutorRepository, PersonaRepository PersonaRepository,
            FileStorageService fileStorageService) {
        this.tutorRepository = tutorRepository;
        this.PersonaRepository = PersonaRepository;
        this.fileStorageService = fileStorageService;
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

    /**
     * Si se envía archivo, lo guarda en disco y actualiza {@link Tutor#getImagen()} para la persona tutor.
     */
    @Transactional
    public void aplicarFotoTutorSiEnviada(Integer idPersona, MultipartFile imagenArchivo) {
        if (imagenArchivo == null || imagenArchivo.isEmpty()) {
            return;
        }
        try {
            Tutor tutor = obtenerOCrearTutorParaPersonaId(idPersona);
            String nueva = fileStorageService.guardarFotoTutor(imagenArchivo);
            fileStorageService.eliminarSiRutaFotoTutor(tutor.getImagen());
            tutor.setImagen(nueva);
            tutorRepository.save(tutor);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la imagen del tutor: " + e.getMessage(), e);
        }
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
