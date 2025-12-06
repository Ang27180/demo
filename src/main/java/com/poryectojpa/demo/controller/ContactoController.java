package com.poryectojpa.demo.controller; // Asegúrate de cambiar el paquete

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
public class ContactoController {

    // 💡 The URL you must use in the browser should match this path!
    // Change this to "/Contacto" if you must use the capital 'C'
   @GetMapping("/Contacto")
public String contacto() {
    return "contacto"; // busca contacto.html
}

}
  