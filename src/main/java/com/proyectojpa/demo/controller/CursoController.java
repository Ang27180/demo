package com.proyectojpa.demo.controller;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectojpa.demo.Service.CertificadoAutorizacionService;
import com.proyectojpa.demo.Service.InscripcionAccesoService;
import com.proyectojpa.demo.Service.ProgresoLeccionService;
import com.proyectojpa.demo.Service.ReciboService;
import com.proyectojpa.demo.domain.InscripcionEstados;
import com.proyectojpa.demo.models.Curso;
import com.proyectojpa.demo.repository.EstudianteRepository;
import com.proyectojpa.demo.repository.InscripcionRepository;
import com.proyectojpa.demo.repository.cursoRepository;
import com.proyectojpa.demo.security.CustomUserDetails;

@Controller
@RequestMapping("/cursos")
public class CursoController {

    @Autowired
    private cursoRepository cursoRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private ProgresoLeccionService progresoLeccionService;

    @Autowired
    private InscripcionAccesoService inscripcionAccesoService;

    @Autowired
    private CertificadoAutorizacionService certificadoAutorizacionService;

    @Autowired
    private ReciboService reciboService;

    @GetMapping
    public String listarCursos(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getPersona().getRolId() != null
                && userDetails.getPersona().getRolId() == 4) {
            return "redirect:/acudiente/panel";
        }
        model.addAttribute("cursos", cursoRepository.findAll());
        return "cursos";
    }

    @GetMapping("/{id}")
    public String verInformacionCurso(@PathVariable("id") Integer id, Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getPersona().getRolId() != null
                && userDetails.getPersona().getRolId() == 4) {
            return "redirect:/acudiente/panel";
        }
        Curso curso = cursoRepository.findByIdWithContenido(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + id));
        model.addAttribute("curso", curso);

        model.addAttribute("puedeRegistrarProgreso", false);
        model.addAttribute("pagoPendiente", false);
        model.addAttribute("inscripcionActual", null);
        model.addAttribute("leccionesCompletadasIds", Collections.emptyList());
        model.addAttribute("progresoCursoPorcentaje", 0);
        model.addAttribute("puedeDescargarCertificado", false);
        model.addAttribute("tieneAcudienteVinculado", false);
        model.addAttribute("mostrarCertificadoDisponible", false);
        model.addAttribute("necesitaAutorizacionAcudienteCertificado", false);

        if (userDetails != null && userDetails.getPersona().getRolId() != null
                && userDetails.getPersona().getRolId() == 2) {
            estudianteRepository.findByPersona(userDetails.getPersona()).ifPresent(est -> inscripcionRepository
                    .findByEstudianteAndCursoWithEstado(est, curso).ifPresent(insc -> {
                        model.addAttribute("inscripcionActual", insc);
                        boolean acceso = inscripcionAccesoService.permiteAccesoContenido(insc.getEstado());
                        boolean pendiente = insc.getEstado() != null && InscripcionEstados.PENDIENTE_PAGO
                                .equals(insc.getEstado().getCodigo());
                        model.addAttribute("pagoPendiente", !acceso && pendiente);
                        model.addAttribute("puedeRegistrarProgreso", acceso);
                        boolean tieneAcudiente = reciboService.tieneAcudienteVinculado(est);
                        model.addAttribute("tieneAcudienteVinculado", tieneAcudiente);
                        if (acceso) {
                            int pct = progresoLeccionService.calcularPorcentaje(est, curso);
                            model.addAttribute("progresoCursoPorcentaje", pct);
                            model.addAttribute("leccionesCompletadasIds",
                                    progresoLeccionService.leccionIdsCompletadas(est, curso.getId()));
                            model.addAttribute("puedeDescargarCertificado",
                                    certificadoAutorizacionService.puedeDescargar(userDetails.getPersona(),
                                            insc.getId()));
                            boolean mostrarCert = pct >= 100;
                            model.addAttribute("mostrarCertificadoDisponible", mostrarCert);
                            model.addAttribute("necesitaAutorizacionAcudienteCertificado",
                                    mostrarCert && tieneAcudiente
                                            && (insc.getCertificadoAutorizado() == null
                                                    || !insc.getCertificadoAutorizado()));
                        }
                    }));
        }

        return "ver-curso";
    }

    @PostMapping("/{cursoId}/lecciones/{leccionId}/completar")
    public String completarLeccion(@PathVariable Integer cursoId, @PathVariable Integer leccionId,
            @AuthenticationPrincipal CustomUserDetails userDetails, RedirectAttributes redirectAttributes) {
        if (userDetails == null || userDetails.getPersona() == null) {
            return "redirect:/login";
        }
        try {
            int pct = progresoLeccionService.marcarLeccionCompletada(userDetails.getPersona(), cursoId, leccionId);
            redirectAttributes.addFlashAttribute("mensajeProgreso",
                    "Lección completada. Avance del curso: " + pct + "%");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorProgreso", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorProgreso", e.getMessage());
        }
        return "redirect:/cursos/" + cursoId;
    }
}
