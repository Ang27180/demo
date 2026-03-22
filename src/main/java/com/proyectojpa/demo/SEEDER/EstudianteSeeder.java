package com.proyectojpa.demo.SEEDER;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.proyectojpa.demo.domain.InscripcionEstados;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.EstadoInscripcionRepository;
import com.proyectojpa.demo.repository.EstudianteRepository;
import com.proyectojpa.demo.repository.PersonaRepository;

@Component
public class EstudianteSeeder implements CommandLineRunner {

    private final EstudianteRepository estudianteRepository;
    private final PersonaRepository PersonaRepository;
    private final EstadoInscripcionRepository estadoInscripcionRepository;

    public EstudianteSeeder(EstudianteRepository estudianteRepository,
                            PersonaRepository PersonaRepository,
                            EstadoInscripcionRepository estadoInscripcionRepository) {
        this.estudianteRepository = estudianteRepository;
        this.PersonaRepository = PersonaRepository;
        this.estadoInscripcionRepository = estadoInscripcionRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        if (estudianteRepository.count() == 0) {

            Persona p1 = PersonaRepository.findById(5).orElse(null);
            Persona p2 = PersonaRepository.findById(6).orElse(null);
            Persona p3 = PersonaRepository.findById(7).orElse(null);

            Estudiante e1 = new Estudiante();
            e1.setProgreso("En curso");
            estadoInscripcionRepository.findByCodigo(InscripcionEstados.ACTIVO).ifPresent(e1::setEstadoEstudiante);
            e1.setPersona(p1);
            estudianteRepository.save(e1);

            Estudiante e2 = new Estudiante();
            e2.setProgreso("Sin terminar");
            estadoInscripcionRepository.findByCodigo(InscripcionEstados.ACTIVO).ifPresent(e2::setEstadoEstudiante);
            e2.setPersona(p2);
            estudianteRepository.save(e2);

            Estudiante e3 = new Estudiante();
            e3.setProgreso("En proceso de validacion");
            estadoInscripcionRepository.findByCodigo(InscripcionEstados.INACTIVO).ifPresent(e3::setEstadoEstudiante);
            e3.setPersona(p3);
            estudianteRepository.save(e3);
        }
    }
}
