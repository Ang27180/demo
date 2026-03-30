package com.proyectojpa.demo.SEEDER;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.proyectojpa.demo.models.Acudiente;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.AcudienteRepository;
import com.proyectojpa.demo.repository.EstudianteRepository;
import com.proyectojpa.demo.repository.PersonaRepository;

/**
 * Seeder de Acudiente. Solo inserta si la tabla está vacía, hay al menos 3 estudiantes
 * y existen las personas 8–10 (acudientes de demo). Debe ejecutarse después de {@link EstudianteSeeder}.
 */
@Component
@Order(40)
@Transactional
public class AcudienteSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AcudienteSeeder.class);

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

        if (acudienteRepository.count() > 0) {
            return;
        }

        List<Estudiante> estudiantes = estudianteRepository.findAll(Sort.by(Sort.Direction.ASC, "idEstudiante"));
        if (estudiantes.size() < 3) {
            log.info("AcudienteSeeder omitido: se requieren al menos 3 filas en estudiante (hay {}).",
                    estudiantes.size());
            return;
        }

        int insertados = 0;
        for (int i = 0; i < 3; i++) {
            int personaId = 8 + i;
            Persona personaAcudiente = personaRepository.findById(personaId).orElse(null);
            Estudiante dependiente = estudiantes.get(i);
            if (personaAcudiente == null || dependiente == null || dependiente.getIdEstudiante() == null) {
                continue;
            }
            Acudiente a = new Acudiente();
            a.setPersona(personaAcudiente);
            a.setEstudianteDependiente(dependiente);
            acudienteRepository.save(a);
            insertados++;
        }

        if (insertados > 0) {
            log.info(">>> AcudienteSeeder: {} vínculo(s) insertado(s).", insertados);
        } else {
            log.info("AcudienteSeeder omitido: faltan personas con id 8, 9 y/o 10 o datos de estudiante.");
        }
    }
}
