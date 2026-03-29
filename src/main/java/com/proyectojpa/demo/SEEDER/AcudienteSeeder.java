package com.proyectojpa.demo.SEEDER;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.proyectojpa.demo.models.Acudiente;
import com.proyectojpa.demo.repository.AcudienteRepository;
import com.proyectojpa.demo.repository.PersonaRepository; // Corregido: mayúscula inicial resuelta
import com.proyectojpa.demo.repository.EstudianteRepository;

/**
 * Seeder de Acudiente. Prepara la base de datos de forma automática en el arranque
 * insertando 3 acudientes si la tabla se encuentra vacía.
 */
@Component
@Transactional
public class AcudienteSeeder implements CommandLineRunner {

    private final AcudienteRepository acudienteRepository;
    private final PersonaRepository personaRepository;
    private final EstudianteRepository estudianteRepository;

    public AcudienteSeeder(
            AcudienteRepository acudienteRepository,
            PersonaRepository personaRepository,
            EstudianteRepository estudianteRepository) {
        this.acudienteRepository = acudienteRepository;
        this.personaRepository = personaRepository;
        this.estudianteRepository = estudianteRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        // Comprueba si hay acudientes creados previamente para evitar duplicados
        if (acudienteRepository.count() > 0) {
            return; 
        }

        // ==============================
        // ACUDIENTE 1 - Persona ID 8, Estudiante ID 1
        // ==============================
        Acudiente a1 = new Acudiente();
        a1.setPersona(personaRepository.findById(8).orElse(null));   
        a1.setEstudianteDependiente(estudianteRepository.findById(1).orElse(null)); 
        acudienteRepository.save(a1);

        // ==============================
        // ACUDIENTE 2 - Persona ID 9, Estudiante ID 2
        // ==============================
        Acudiente a2 = new Acudiente();
        a2.setPersona(personaRepository.findById(9).orElse(null));   
        a2.setEstudianteDependiente(estudianteRepository.findById(2).orElse(null)); 
        acudienteRepository.save(a2);

        // ==============================
        // ACUDIENTE 3 - Persona ID 10, Estudiante ID 3
        // ==============================
        Acudiente a3 = new Acudiente();
        a3.setPersona(personaRepository.findById(10).orElse(null));  
        a3.setEstudianteDependiente(estudianteRepository.findById(3).orElse(null)); 
        acudienteRepository.save(a3);

        System.out.println(">>> Acudientes insertados correctamente");
    }
}
