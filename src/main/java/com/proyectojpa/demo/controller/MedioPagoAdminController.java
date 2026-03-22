package com.proyectojpa.demo.controller;



import org.springframework.http.MediaType;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.PutMapping;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;



import com.proyectojpa.demo.Service.FileStorageService;

import com.proyectojpa.demo.models.MedioPago;

import com.proyectojpa.demo.repository.MedioPagoRepository;

import com.proyectojpa.demo.repository.ReciboRepository;

import com.proyectojpa.demo.security.CustomUserDetails;



@Controller

@RequestMapping("/admin/medios-pago")

public class MedioPagoAdminController {



    private final MedioPagoRepository medioPagoRepository;

    private final ReciboRepository reciboRepository;

    private final FileStorageService fileStorageService;



    public MedioPagoAdminController(MedioPagoRepository medioPagoRepository,

            ReciboRepository reciboRepository,

            FileStorageService fileStorageService) {

        this.medioPagoRepository = medioPagoRepository;

        this.reciboRepository = reciboRepository;

        this.fileStorageService = fileStorageService;

    }



    @GetMapping

    public String listar(Model model) {

        model.addAttribute("medios", medioPagoRepository.findAllWithAdmin());

        model.addAttribute("medioNuevo", new MedioPago());

        return "admin/medios-pago";

    }



    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    public String guardar(

            @RequestParam String nombre,

            @RequestParam String tipo,

            @RequestParam(required = false) String imagenQr,

            @RequestParam(required = false) MultipartFile imagenArchivo,

            @AuthenticationPrincipal CustomUserDetails userDetails,

            RedirectAttributes redirectAttributes) throws java.io.IOException {

        if (userDetails == null || userDetails.getPersona() == null) {

            return "redirect:/login";

        }

        MedioPago medio = new MedioPago();

        medio.setNombre(nombre.trim());

        medio.setTipo(tipo.trim());

        medio.setAdminPersona(userDetails.getPersona());



        if (imagenArchivo != null && !imagenArchivo.isEmpty()) {

            medio.setImagenQr(fileStorageService.guardarImagenMedioPago(imagenArchivo));

        } else if (imagenQr != null && !imagenQr.isBlank()) {

            medio.setImagenQr(imagenQr.trim());

        }



        medioPagoRepository.save(medio);

        redirectAttributes.addFlashAttribute("msgAdmin", "Medio de pago guardado.");

        return "redirect:/admin/medios-pago";

    }



    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    public String actualizar(

            @PathVariable Integer id,

            @RequestParam String nombre,

            @RequestParam String tipo,

            @RequestParam(required = false) String imagenQr,

            @RequestParam(required = false) MultipartFile imagenArchivo,

            @AuthenticationPrincipal CustomUserDetails userDetails,

            RedirectAttributes redirectAttributes) throws java.io.IOException {

        if (userDetails == null || userDetails.getPersona() == null) {

            return "redirect:/login";

        }

        MedioPago medio = medioPagoRepository.findById(id).orElseThrow();

        medio.setNombre(nombre.trim());

        medio.setTipo(tipo.trim());



        if (imagenArchivo != null && !imagenArchivo.isEmpty()) {

            fileStorageService.eliminarSiRutaArchivoLocal(medio.getImagenQr());

            medio.setImagenQr(fileStorageService.guardarImagenMedioPago(imagenArchivo));

        } else if (imagenQr != null && !imagenQr.isBlank()) {

            fileStorageService.eliminarSiRutaArchivoLocal(medio.getImagenQr());

            medio.setImagenQr(imagenQr.trim());

        }



        medioPagoRepository.save(medio);

        redirectAttributes.addFlashAttribute("msgAdmin", "Medio de pago actualizado.");

        return "redirect:/admin/medios-pago";

    }



    @DeleteMapping("/{id}")

    public String eliminar(@PathVariable Integer id,

            @AuthenticationPrincipal CustomUserDetails userDetails,

            RedirectAttributes redirectAttributes) {

        if (userDetails == null || userDetails.getPersona() == null) {

            return "redirect:/login";

        }

        MedioPago medio = medioPagoRepository.findById(id).orElseThrow();

        if (reciboRepository.existsByMedioPagoId(id)) {

            redirectAttributes.addFlashAttribute("errorAdmin",

                    "No se puede eliminar: hay recibos vinculados a este medio de pago.");

            return "redirect:/admin/medios-pago";

        }

        fileStorageService.eliminarSiRutaArchivoLocal(medio.getImagenQr());

        medioPagoRepository.delete(medio);

        redirectAttributes.addFlashAttribute("msgAdmin", "Medio de pago eliminado.");

        return "redirect:/admin/medios-pago";

    }

}

