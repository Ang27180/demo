package com.poryectojpa.demo.Service;
import com.poryectojpa.demo.dto.DatoEstadisticoDTO;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteJasperService {

    public byte[] generarReporteEstadisticoPdf(
            List<DatoEstadisticoDTO> datos,
            Map<String, Object> parametros
    ) {
        try {
            // OJO: sin pasar la ruta desde afuera,
            // la dejamos fija y bien escrita
            InputStream jrxmlStream = getClass()
                    .getResourceAsStream("/Reportes/Reporte_Estadistico.jrxml");

            if (jrxmlStream == null) {
                throw new RuntimeException("No se encontró la plantilla: /Reportes/Reporte_Estadistico.jrxml");
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(datos);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);
            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generando el reporte Jasper: " + e.getMessage(), e);
        }
    }

    public byte[] generarCertificadoPdf(Map<String, Object> parametros) {
        try {
            // Desactivar validación XML para evitar errores de red o de parseo estrictos
            System.setProperty("net.sf.jasperreports.xml.validation", "false");
            
            String version = net.sf.jasperreports.engine.JasperCompileManager.class.getPackage().getImplementationVersion();
            System.out.println("[DEBUG] Ejecutando con JasperReports versión: " + version);
            
            System.out.println("[DEBUG] Iniciando generación de certificado...");
            ClassPathResource resource = new ClassPathResource("Reportes/Certificado.jrxml");
            
            if (!resource.exists()) {
                throw new RuntimeException("No se encontró el archivo en: resources/Reportes/Certificado.jrxml");
            }

            // Leemos el archivo a un String para asegurar que el contenido sea válido antes de pasarlo a Jasper
            String contenidoXml;
            try (InputStream is = resource.getInputStream()) {
                contenidoXml = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }

            // Depuración: Mostrar los primeros 100 caracteres en consola
            System.out.println("[DEBUG] Contenido del reporte (primeros 100 caracteres): " + 
                (contenidoXml.length() > 100 ? contenidoXml.substring(0, 100) : contenidoXml));

            if (!contenidoXml.trim().startsWith("<")) {
                throw new RuntimeException("El archivo JRXML no parece un XML válido o está vacío.");
            }

            System.out.println("[DEBUG] Compilando reporte desde String...");
            // Compilamos desde el stream de la cadena de texto
            InputStream xmlStream = new java.io.ByteArrayInputStream(contenidoXml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            JasperReport jasperReport = JasperCompileManager.compileReport(xmlStream);
            
            System.out.println("[DEBUG] Llenando reporte...");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, new JREmptyDataSource());
            
            System.out.println("[DEBUG] Exportando a PDF...");
            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (Exception e) {
            System.err.println("[ERROR JASPER] Fallo total: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al generar el certificado: " + e.getMessage(), e);
        }
    }
}