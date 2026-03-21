package com.poryectojpa.demo.SEEDER;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.poryectojpa.demo.models.Acudiente;
import com.poryectojpa.demo.models.Persona;
import com.poryectojpa.demo.models.Estudiante;
import com.poryectojpa.demo.repository.AcudienteRepository;
import com.poryectojpa.demo.repository.personaRepository;
import com.poryectojpa.demo.repository.EstudianteRepository;

@Component
@Transactional
public class AcudienteSeeder implements CommandLineRunner {

    private final AcudienteRepository acudienteRepository;
    private final personaRepository personaRepository;
    private final EstudianteRepository estudianteRepository;

    public AcudienteSeeder(
            AcudienteRepository acudienteRepository,
            personaRepository personaRepository,
            EstudianteRepository estudianteRepository) {
        this.acudienteRepository = acudienteRepository;
        this.personaRepository = personaRepository;
        this.estudianteRepository = estudianteRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        if (acudienteRepository.count() > 0) {
            return; // Ya existen acudientes
        }

        // ==============================
        // ACUDIENTE 1
        // ==============================
        Acudiente a1 = new Acudiente();
        a1.setAutorizacion("Autorizado total");
        a1.setPersona(personaRepository.findById(8).orElse(null));   // Valentina López
        a1.setEstudianteDependiente(estudianteRepository.findById(1).orElse(null)); // Estudiante 1
        acudienteRepository.save(a1);

        // ==============================
        // ACUDIENTE 2
        // ==============================
        Acudiente a2 = new Acudiente();
        a2.setAutorizacion("Acceso parcial");
        a2.setPersona(personaRepository.findById(9).orElse(null));   // Emilio Suárez
        a2.setEstudianteDependiente(estudianteRepository.findById(2).orElse(null)); // Estudiante 2
        acudienteRepository.save(a2);

        // ==============================
        // ACUDIENTE 3
        // ==============================
        Acudiente a3 = new Acudiente();
        a3.setAutorizacion("Acceso restringido");
        a3.setPersona(personaRepository.findById(10).orElse(null));  // Gabriela Ortega
        a3.setEstudianteDependiente(estudianteRepository.findById(3).orElse(null)); // Estudiante 3
        acudienteRepository.save(a3);

        System.out.println(">>> Acudientes insertados correctamente");
    }
}
