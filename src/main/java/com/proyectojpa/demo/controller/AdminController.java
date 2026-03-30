package com.proyectojpa.demo.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.proyectojpa.demo.domain.InscripcionEstados;
import com.proyectojpa.demo.models.Curso;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.Service.TutorService;
import com.proyectojpa.demo.repository.EstadoInscripcionRepository;
import com.proyectojpa.demo.repository.EstudianteRepository;
import com.proyectojpa.demo.repository.PersonaRepository;
import com.proyectojpa.demo.repository.cursoRepository;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class AdminController {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private cursoRepository cursoRepository;

    @Autowired
    private TutorService tutorService;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private EstadoInscripcionRepository estadoInscripcionRepository;

    // PANEL ADMIN CON FILTROS
    @GetMapping("/admin")
    public String mostrarPanelAdmin(
            // CORRECCIÓN: name= explícito requerido en Spring Boot 3.2+ sin flag -parameters
            @RequestParam(name = "filtroNombre", required = false) String filtroNombre,
            @RequestParam(name = "filtroRol", required = false) Integer filtroRol,
            @RequestParam(name = "filtroNombreCurso", required = false) String filtroNombreCurso,
            @RequestParam(name = "filtroIdPersonaTutor", required = false) Integer filtroIdPersonaTutor,
            Model model) {

        // inicializar si vienen nulos
        final String filtroNombreFinal = (filtroNombre == null) ? "" : filtroNombre;
        final String filtroNombreCursoFinal = (filtroNombreCurso == null) ? "" : filtroNombreCurso.trim();

        // Carga con EntityGraph para estudiantes/estado (vista admin con open-in-view=false)
        List<Persona> personas = personaRepository.findAllByOrderByIdAsc().stream()
                .filter(p -> filtroNombreFinal.isEmpty() || p.getNombre().toLowerCase().contains(filtroNombreFinal.toLowerCase()))
                .filter(p -> filtroRol == null || p.getRolId() != null && p.getRolId().equals(filtroRol))
                .toList();

        List<Curso> cursos = cursoRepository.findAllWithTutorPersonaOrderById().stream()
                .filter(c -> filtroNombreCursoFinal.isEmpty()
                        || (c.getNombre() != null
                                && c.getNombre().toLowerCase().contains(filtroNombreCursoFinal.toLowerCase())))
                .filter(c -> filtroIdPersonaTutor == null
                        || (c.getTutor() != null && c.getTutor().getPersona() != null
                                && filtroIdPersonaTutor.equals(c.getTutor().getPersona().getId())))
                .toList();

        model.addAttribute("personas", personas);
        model.addAttribute("personasPorRol", agruparPersonasPorRol(personas));
        model.addAttribute("cursos", cursos);

        // valores para mantener el filtro en la vista
        model.addAttribute("filtroNombre", filtroNombre);
        model.addAttribute("filtroRol", filtroRol);
        model.addAttribute("filtroNombreCurso", filtroNombreCurso);
        model.addAttribute("filtroIdPersonaTutor", filtroIdPersonaTutor);
        model.addAttribute("personasTutorSelect", tutorService.listarPersonasRolTutor());

        // Tarjetas resumen dinámicas
        model.addAttribute("totalUsuarios", personaRepository.count());
        model.addAttribute("totalCursos", cursoRepository.count());
        model.addAttribute("totalTutores", personaRepository.countByRolId(3));
        model.addAttribute("totalEstudiantes", personaRepository.countByRolId(2));

        return "admin";
    }

    /** Activa/desactiva cuenta de estudiante desde el panel admin (sin redirección). */
    @PostMapping("/admin/personas/{id}/toggle-estado-cuenta")
    @ResponseBody
    public ResponseEntity<Map<String, String>> toggleEstadoCuentaEstudianteAjax(@PathVariable Integer id) {
        Map<String, String> body = new HashMap<>();
        Persona p = personaRepository.findById(id).orElse(null);
        if (p == null) {
            body.put("error", "Persona no encontrada");
            return ResponseEntity.status(404).body(body);
        }
        if (p.getRolId() == null || p.getRolId() != 2) {
            body.put("error", "Solo aplica a estudiantes");
            return ResponseEntity.badRequest().body(body);
        }
        Estudiante e = estudianteRepository.findByPersona(p).orElse(null);
        if (e == null) {
            body.put("error", "Sin registro de estudiante");
            return ResponseEntity.badRequest().body(body);
        }
        boolean esActivo = e.getEstadoEstudiante() != null
                && InscripcionEstados.ACTIVO.equals(e.getEstadoEstudiante().getCodigo());
        String nuevoCodigo = esActivo ? InscripcionEstados.INACTIVO : InscripcionEstados.ACTIVO;
        estadoInscripcionRepository.findByCodigo(nuevoCodigo).ifPresent(est -> {
            e.setEstadoEstudiante(est);
            estudianteRepository.save(e);
        });
        body.put("estadoCodigo", nuevoCodigo);
        return ResponseEntity.ok(body);
    }

    private static Map<String, List<Persona>> agruparPersonasPorRol(List<Persona> personas) {
        Map<String, List<Persona>> map = new LinkedHashMap<>();
        // Nota: LinkedHashMap respeta el orden de inserción; el usuario pidió que
        // "Estudiantes" quede al final de la tabla.
        putSiNoVacio(map, "Administradores",
                personas.stream().filter(p -> p.getRolId() != null && p.getRolId() == 1).collect(Collectors.toList()));
        putSiNoVacio(map, "Tutores",
                personas.stream().filter(p -> p.getRolId() != null && p.getRolId() == 3).collect(Collectors.toList()));
        putSiNoVacio(map, "Acudientes",
                personas.stream().filter(p -> p.getRolId() != null && p.getRolId() == 4).collect(Collectors.toList()));
        putSiNoVacio(map, "Otros / sin rol",
                personas.stream().filter(p -> p.getRolId() == null || p.getRolId() < 1 || p.getRolId() > 4)
                        .collect(Collectors.toList()));
        putSiNoVacio(map, "Estudiantes",
                personas.stream().filter(p -> p.getRolId() != null && p.getRolId() == 2).collect(Collectors.toList()));
        return map;
    }

    private static void putSiNoVacio(Map<String, List<Persona>> map, String etiqueta, List<Persona> lista) {
        if (!lista.isEmpty()) {
            map.put(etiqueta, lista);
        }
    }

    // EXPORTAR EXCEL CON FILTROS
    @GetMapping("/personas/exportarExcel")
    public void exportarExcel(
            HttpServletResponse response,
            @RequestParam(name = "filtroNombre", required = false) String filtroNombre,
            @RequestParam(name = "filtroRol", required = false) Integer filtroRol,
            @RequestParam(name = "filtroNombreCurso", required = false) String filtroNombreCurso,
            @RequestParam(name = "filtroIdPersonaTutor", required = false) Integer filtroIdPersonaTutor) throws IOException {

        final String filtroNombreFinal = (filtroNombre == null) ? "" : filtroNombre;
        final String filtroNombreCursoFinal = (filtroNombreCurso == null) ? "" : filtroNombreCurso.trim();

        List<Persona> personas = personaRepository.findAllByOrderByIdAsc().stream()
                .filter(p -> filtroNombreFinal.isEmpty() || p.getNombre().toLowerCase().contains(filtroNombreFinal.toLowerCase()))
                .filter(p -> filtroRol == null || p.getRolId() != null && p.getRolId().equals(filtroRol))
                .toList();

        List<Curso> cursos = cursoRepository.findAllWithTutorPersonaOrderById().stream()
                .filter(c -> filtroNombreCursoFinal.isEmpty()
                        || (c.getNombre() != null
                                && c.getNombre().toLowerCase().contains(filtroNombreCursoFinal.toLowerCase())))
                .filter(c -> filtroIdPersonaTutor == null
                        || (c.getTutor() != null && c.getTutor().getPersona() != null
                                && filtroIdPersonaTutor.equals(c.getTutor().getPersona().getId())))
                .toList();

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
                    case 4 -> "Acudiente";
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
