package com.proyectojpa.demo.controller;

import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectojpa.demo.Service.EmailService;
import com.proyectojpa.demo.models.Curso;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.cursoRepository;
import com.proyectojpa.demo.repository.PersonaRepository;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class AdminController {

    @Autowired
    private PersonaRepository PersonaRepository;

    @Autowired
    private cursoRepository cursoRepository;

    @Autowired
    private com.proyectojpa.demo.repository.InscripcionRepository inscripcionRepository;

    @Autowired
    private EmailService emailService;

    // PANEL ADMIN CON FILTROS
    @GetMapping("/admin")
    public String mostrarPanelAdmin(
            @RequestParam(name = "filtroNombre", required = false) String filtroNombre,
            @RequestParam(name = "filtroRol", required = false) String filtroRolParam,
            Model model) {

        final String filtroNombreFinal = (filtroNombre == null) ? "" : filtroNombre;
        final Integer filtroRol = parseFiltroRol(filtroRolParam);

        // aplicar filtros en memoria (JOIN FETCH estudiante/estado para columnas y toggle)
        List<Persona> personas = PersonaRepository.findAllWithEstudianteEstado().stream()
                .filter(p -> filtroNombreFinal.isEmpty() || p.getNombre().toLowerCase().contains(filtroNombreFinal.toLowerCase()))
                .filter(p -> filtroRol == null || p.getRolId() != null && p.getRolId().equals(filtroRol))
                .toList();

        List<Curso> cursos = cursoRepository.findAll();

        model.addAttribute("personas", personas);
        model.addAttribute("cursos", cursos);

        // valores para mantener el filtro en la vista
        model.addAttribute("filtroNombre", filtroNombre);
        model.addAttribute("filtroRol", filtroRol);

        // Tarjetas resumen dinámicas
        model.addAttribute("totalUsuarios", PersonaRepository.count());
        model.addAttribute("totalCursos", cursoRepository.count());
        model.addAttribute("totalTutores", PersonaRepository.countByRolId(3));
        model.addAttribute("nuevasInscripciones", inscripcionRepository.count());

        return "admin";
    }

    // EXPORTAR EXCEL CON FILTROS
    @GetMapping("/personas/exportarExcel")
    public void exportarExcel(
            HttpServletResponse response,
            @RequestParam(name = "filtroNombre", required = false) String filtroNombre,
            @RequestParam(name = "filtroRol", required = false) String filtroRolParam) throws IOException {

        final String filtroNombreFinal = (filtroNombre == null) ? "" : filtroNombre;
        final Integer filtroRol = parseFiltroRol(filtroRolParam);

        List<Persona> personas = PersonaRepository.findAllWithEstudianteEstado().stream()
                .filter(p -> filtroNombreFinal.isEmpty() || p.getNombre().toLowerCase().contains(filtroNombreFinal.toLowerCase()))
                .filter(p -> filtroRol == null || p.getRolId() != null && p.getRolId().equals(filtroRol))
                .toList();

        List<Curso> cursos = cursoRepository.findAll();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=admin_report.xlsx");

        Workbook workbook = new XSSFWorkbook();

        // --- Hoja 1: Usuarios ---
        Sheet sheetUsuarios = workbook.createSheet("Usuarios");
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        String[] columnasUsuarios = { "ID", "Documento", "Tipo Documento", "Nombre", "Email", "Rol" };
        Row headerUsuarios = sheetUsuarios.createRow(0);
        for (int i = 0; i < columnasUsuarios.length; i++) {
            Cell cell = headerUsuarios.createCell(i);
            cell.setCellValue(columnasUsuarios[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Persona p : personas) {
            Row row = sheetUsuarios.createRow(rowNum++);
            row.createCell(0).setCellValue(p.getId() != null ? p.getId() : 0);
            row.createCell(1).setCellValue(p.getDocumento() != null ? p.getDocumento() : "");
            row.createCell(2).setCellValue(p.getTipoDocumento() != null ? p.getTipoDocumento() : "");
            row.createCell(3).setCellValue(p.getNombre() != null ? p.getNombre() : "");
            row.createCell(4).setCellValue(p.getEmail() != null ? p.getEmail() : "");

            String rolTexto;
            if (p.getRolId() == null) {
                rolTexto = "Sin rol";
            } else {
                rolTexto = switch (p.getRolId()) {
                    case 1 -> "Administrador";
                    case 2 -> "Estudiante";
                    case 3 -> "Tutor";
                    case 4 -> "Proveedor";
                    default -> "Desconocido";
                };
            }
            row.createCell(5).setCellValue(rolTexto);
        }
        for (int i = 0; i < columnasUsuarios.length; i++) {
            sheetUsuarios.autoSizeColumn(i);
        }

        // --- Hoja 2: Cursos ---
        Sheet sheetCursos = workbook.createSheet("Cursos");
        String[] columnasCursos = { "ID", "Nombre", "Duración", "Número Curso", "Detalle", "Costo", "Nivel", "Categoría" };
        Row headerCursos = sheetCursos.createRow(0);
        for (int i = 0; i < columnasCursos.length; i++) {
            Cell cell = headerCursos.createCell(i);
            cell.setCellValue(columnasCursos[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowCursoNum = 1;
        for (Curso c : cursos) {
            Row row = sheetCursos.createRow(rowCursoNum++);
            row.createCell(0).setCellValue(c.getId() != null ? c.getId() : 0);
            row.createCell(1).setCellValue(c.getNombre() != null ? c.getNombre() : "");
            row.createCell(2).setCellValue(c.getDuracion() != null ? c.getDuracion() : "");
            row.createCell(3).setCellValue(c.getNumcurso() != null ? c.getNumcurso() : "");
            row.createCell(4).setCellValue(c.getDetalle() != null ? c.getDetalle() : "");
            row.createCell(5).setCellValue(c.getCosto() != null ? c.getCosto() : 0);
            row.createCell(6).setCellValue(c.getAprendizaje() != null ? c.getAprendizaje() : "");
            row.createCell(7).setCellValue(c.getCategoria() != null ? c.getCategoria().toString() : "");
        }
        for (int i = 0; i < columnasCursos.length; i++) {
            sheetCursos.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/admin/correo-curso")
    public String formCorreoPorCurso(Model model) {
        model.addAttribute("cursos", cursoRepository.findAll());
        return "admin-correo-curso";
    }

    @PostMapping("/admin/correo-curso/enviar")
    public String enviarCorreoPorCurso(@RequestParam Integer idCurso, @RequestParam String asunto,
            @RequestParam String cuerpo, RedirectAttributes redirectAttributes) {
        var list = inscripcionRepository.findByCursoIdWithEstudianteAndEstado(idCurso);
        int enviados = 0;
        for (var i : list) {
            String email = i.getEstudiante().getPersona().getEmail();
            if (email != null && !email.isBlank()) {
                try {
                    String html = "<p>" + cuerpo.replace("\n", "<br>") + "</p>";
                    emailService.enviarHtml(email, asunto, html);
                    enviados++;
                } catch (Exception ex) {
                    redirectAttributes.addFlashAttribute("errorCorreo", ex.getMessage());
                    return "redirect:/admin/correo-curso";
                }
            }
        }
        redirectAttributes.addFlashAttribute("okCorreo", "Correos enviados a estudiantes del curso: " + enviados);
        return "redirect:/admin";
    }

    private static Integer parseFiltroRol(String filtroRolParam) {
        if (filtroRolParam == null || filtroRolParam.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(filtroRolParam.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
