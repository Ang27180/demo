package com.proyectojpa.demo.SEEDER;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.EstudianteRepository;
import com.proyectojpa.demo.repository.PersonaRepository;

@Component
public class EstudianteSeeder implements CommandLineRunner {

    private final EstudianteRepository estudianteRepository;
    private final PersonaRepository PersonaRepository;

    public EstudianteSeeder(EstudianteRepository estudianteRepository,
                            PersonaRepository PersonaRepository) {
        this.estudianteRepository = estudianteRepository;
        this.PersonaRepository = PersonaRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        if (estudianteRepository.count() == 0) {

            Persona p1 = PersonaRepository.findById(5).orElse(null);
            Persona p2 = PersonaRepository.findById(6).orElse(null);
            Persona p3 = PersonaRepository.findById(7).orElse(null);

            Estudiante e1 = new Estudiante();
            //e1.setContraseña("1234");
            e1.setProgreso("En curso");
            e1.setEstadoEstudiante(1);
            e1.setPersona(p1);
            estudianteRepository.save(e1);

            Estudiante e2 = new Estudiante();
            //e2.setContraseña("1235");
            e2.setProgreso("Sin terminar");
            e2.setEstadoEstudiante(2);
            e2.setPersona(p2);
            estudianteRepository.save(e2);

            Estudiante e3 = new Estudiante();
            //e3.setContraseña("1236");
            e3.setProgreso("En proceso de validacion");
            e3.setEstadoEstudiante(3);
            e3.setPersona(p3);
            estudianteRepository.save(e3);
        }
    }
}
