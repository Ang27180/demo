package com.poryectojpa.demo.SEEDER;


import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.poryectojpa.demo.models.Persona;
import com.poryectojpa.demo.models.Tutor;
import com.poryectojpa.demo.repository.TutorRepository;
import com.poryectojpa.demo.repository.personaRepository;

@Component
public class TutorSeeder implements CommandLineRunner {

    private final TutorRepository tutorRepository;
    private final personaRepository personaRepository;

    public TutorSeeder(TutorRepository tutorRepository, personaRepository personaRepository) {
        this.tutorRepository = tutorRepository;
        this.personaRepository = personaRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        if (tutorRepository.count() == 0) {

            Persona p1 = personaRepository.findById(1).orElse(null);

            Tutor t = new Tutor();
            t.setExperiencia("5 años enseñando cocina");
            t.setObservaciones("Especialista en repostería");
            t.setPersona(p1);

            tutorRepository.save(t);

            System.out.println(">>> Tutor inicial creado");
        }
    }
}

