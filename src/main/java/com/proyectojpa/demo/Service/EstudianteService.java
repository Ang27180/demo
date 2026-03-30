package com.proyectojpa.demo.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.proyectojpa.demo.dto.EstudianteDTO;
import com.proyectojpa.demo.models.Acudiente;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.AcudienteRepository;
import com.proyectojpa.demo.repository.EstadoInscripcionRepository;
import com.proyectojpa.demo.repository.EstudianteRepository;
import com.proyectojpa.demo.repository.PersonaRepository;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final PersonaRepository PersonaRepository;
    private final EstadoInscripcionRepository estadoInscripcionRepository;
    private final AcudienteRepository acudienteRepository;

    public EstudianteService(EstudianteRepository estudianteRepository,
                             PersonaRepository PersonaRepository,
                             EstadoInscripcionRepository estadoInscripcionRepository,
                             AcudienteRepository acudienteRepository) {
        this.estudianteRepository = estudianteRepository;
        this.PersonaRepository = PersonaRepository;
        this.estadoInscripcionRepository = estadoInscripcionRepository;
        this.acudienteRepository = acudienteRepository;
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
            if (e.getIdEstudiante() != null) {
                List<Acudiente> vinculos = acudienteRepository.findByEstudianteDependienteIdEstudiante(e.getIdEstudiante());
                if (!vinculos.isEmpty() && vinculos.get(0).getPersona() != null) {
                    Persona ap = vinculos.get(0).getPersona();
                    dto.setTutorNombre(ap.getNombre());
                    dto.setTutorTelefono(ap.getTelefono());
                    dto.setTutorEmail(ap.getEmail());
                }
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
        if (p.getRolId() == null || p.getRolId() != 2) {
            throw new IllegalArgumentException("Solo personas con rol estudiante (id_rol=2) pueden tener fila en estudiante");
        }

        e.setPersona(p);

        estudianteRepository.save(e);

        if (dto.getTutorNombre() != null || dto.getTutorTelefono() != null || dto.getTutorEmail() != null) {
            List<Acudiente> vinculos = acudienteRepository.findByEstudianteDependienteIdEstudiante(e.getIdEstudiante());
            if (!vinculos.isEmpty() && vinculos.get(0).getPersona() != null) {
                Persona ap = vinculos.get(0).getPersona();
                if (dto.getTutorNombre() != null) {
                    ap.setNombre(dto.getTutorNombre());
                }
                if (dto.getTutorTelefono() != null) {
                    ap.setTelefono(dto.getTutorTelefono());
                }
                if (dto.getTutorEmail() != null) {
                    ap.setEmail(dto.getTutorEmail());
                }
                PersonaRepository.save(ap);
            }
        }

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
        if (e.getPersona() != null) {
            Persona p = e.getPersona();
            if (p.getRolId() == null || p.getRolId() != 2) {
                throw new IllegalArgumentException("Solo personas con rol estudiante (id_rol=2) pueden tener fila en estudiante");
            }
        }
        estudianteRepository.save(e);
    }

    public void delete(Integer id) {
        estudianteRepository.deleteById(id);
    }

    public java.util.Optional<Estudiante> findByPersona(Persona p) {
        return estudianteRepository.findByPersona(p);
    }
}

