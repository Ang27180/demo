package com.poryectojpa.demo.controller;

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
import org.springframework.web.bind.annotation.RequestParam;

import com.poryectojpa.demo.models.Curso;
import com.poryectojpa.demo.models.Persona;
import com.poryectojpa.demo.repository.cursoRepository;
import com.poryectojpa.demo.repository.personaRepository;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class AdminController {

    @Autowired
    private personaRepository personaRepository;

    @Autowired
    private cursoRepository cursoRepository;

    @Autowired
    private com.poryectojpa.demo.repository.InscripcionRepository inscripcionRepository;

    // PANEL ADMIN CON FILTROS
    @GetMapping("/admin")
    public String mostrarPanelAdmin(
            @RequestParam(required = false) String filtroNombre,
            @RequestParam(required = false) Integer filtroRol,
            Model model) {

        // inicializar si vienen nulos
        final String filtroNombreFinal = (filtroNombre == null) ? "" : filtroNombre;
        // filtroRol puede quedarse como null

        // aplicar filtros en memoria
        List<Persona> personas = personaRepository.findAll().stream()
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
        model.addAttribute("totalUsuarios", personaRepository.count());
        model.addAttribute("totalCursos", cursoRepository.count());
        model.addAttribute("totalTutores", personaRepository.countByRolId(3));
        model.addAttribute("nuevasInscripciones", inscripcionRepository.count());

        return "admin";
    }

    // EXPORTAR EXCEL CON FILTROS
    @GetMapping("/personas/exportarExcel")
    public void exportarExcel(
            HttpServletResponse response,
            @RequestParam(required = false) String filtroNombre,
            @RequestParam(required = false) Integer filtroRol) throws IOException {

        final String filtroNombreFinal = (filtroNombre == null) ? "" : filtroNombre;

        List<Persona> personas = personaRepository.findAll().stream()
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
}
