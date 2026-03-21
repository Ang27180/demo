package com.proyectojpa.demo.SEEDER;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.models.Tutor;
import com.proyectojpa.demo.repository.TutorRepository;
import com.proyectojpa.demo.repository.PersonaRepository;

@Component
public class TutorSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TutorSeeder.class);

    private final TutorRepository tutorRepository;
    private final PersonaRepository PersonaRepository;

    public TutorSeeder(TutorRepository tutorRepository, PersonaRepository PersonaRepository) {
        this.tutorRepository = tutorRepository;
        this.PersonaRepository = PersonaRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        if (tutorRepository.count() == 0) {

            Persona p1 = PersonaRepository.findById(1).orElse(null);

            Tutor t = new Tutor();
            t.setExperiencia("5 años enseñando cocina");
            t.setObservaciones("Especialista en repostería");
            t.setPersona(p1);

            tutorRepository.save(t);

            log.info("Tutor inicial creado");
        }
    }
}

