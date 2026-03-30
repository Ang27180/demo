package com.proyectojpa.demo.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HexFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.proyectojpa.demo.domain.InscripcionEstados;
import com.proyectojpa.demo.domain.OrdenPagoEstados;
import com.proyectojpa.demo.models.ComprobantePago;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.HistorialPago;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.OrdenPago;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.ComprobantePagoRepository;
import com.proyectojpa.demo.repository.EstadoInscripcionRepository;
import com.proyectojpa.demo.repository.EstudianteRepository;
import com.proyectojpa.demo.repository.HistorialPagoRepository;
import com.proyectojpa.demo.repository.InscripcionRepository;
import com.proyectojpa.demo.repository.OrdenPagoRepository;

@Service
public class OrdenPagoService {

    private static final Set<String> ACTIVOS = Set.of(
            OrdenPagoEstados.PENDIENTE,
            OrdenPagoEstados.COMPROBANTE_CARGADO,
            OrdenPagoEstados.EN_REVISION);

    private static final Set<String> MIME_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "application/pdf");

    private final OrdenPagoRepository ordenPagoRepository;
    private final ComprobantePagoRepository comprobantePagoRepository;
    private final HistorialPagoRepository historialPagoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final EstudianteRepository estudianteRepository;
    private final EstadoInscripcionRepository estadoInscripcionRepository;

    public OrdenPagoService(OrdenPagoRepository ordenPagoRepository,
            ComprobantePagoRepository comprobantePagoRepository,
            HistorialPagoRepository historialPagoRepository,
            InscripcionRepository inscripcionRepository,
            EstudianteRepository estudianteRepository,
            EstadoInscripcionRepository estadoInscripcionRepository) {
        this.ordenPagoRepository = ordenPagoRepository;
        this.comprobantePagoRepository = comprobantePagoRepository;
        this.historialPagoRepository = historialPagoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.estudianteRepository = estudianteRepository;
        this.estadoInscripcionRepository = estadoInscripcionRepository;
    }

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public OrdenPago obtenerOrdenActivaPorInscripcion(Integer idInscripcion) {
        return ordenPagoRepository
                .findFirstByInscripcion_IdAndEstadoInOrderByIdDesc(idInscripcion, ACTIVOS)
                .orElse(null);
    }

    /** Una orden activa por id de inscripción (la más reciente). */
    @Transactional(readOnly = true)
    public Map<Integer, OrdenPago> mapaOrdenActivaPorInscripciones(Integer idEstudiante) {
        List<OrdenPago> list = ordenPagoRepository.findActivasPorEstudiante(idEstudiante, ACTIVOS);
        Map<Integer, OrdenPago> map = new HashMap<>();
        for (OrdenPago o : list) {
            map.putIfAbsent(o.getInscripcion().getId(), o);
        }
        return map;
    }

    @Transactional
    public OrdenPago crearOrdenNequi(Persona estudiantePersona, Integer idInscripcion) {
        Estudiante estudiante = estudianteRepository.findByPersona(estudiantePersona)
                .orElseThrow(() -> new IllegalStateException("No hay perfil de estudiante"));

        Inscripcion insc = inscripcionRepository.findByIdWithEstudiantePersonaAndCurso(idInscripcion)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));

        if (!estudiante.getIdEstudiante().equals(insc.getEstudiante().getIdEstudiante())) {
            throw new IllegalStateException("La inscripción no pertenece al estudiante");
        }
        if (insc.getEstado() == null || !InscripcionEstados.PENDIENTE_PAGO.equals(insc.getEstado().getCodigo())) {
            throw new IllegalStateException("Solo se genera orden con inscripción pendiente de pago");
        }
        if (ordenPagoRepository.existsByInscripcion_IdAndEstadoIn(insc.getId(), ACTIVOS)) {
            throw new IllegalStateException("Ya existe una orden de pago activa para esta inscripción");
        }

        BigDecimal monto = BigDecimal.valueOf(insc.getCurso().getCosto() != null ? insc.getCurso().getCosto() : 0.0)
                .setScale(2, RoundingMode.HALF_UP);

        OrdenPago o = new OrdenPago();
        o.setInscripcion(insc);
        o.setConcepto("Matrícula: " + (insc.getCurso().getNombre() != null ? insc.getCurso().getNombre() : "Curso"));
        o.setMonto(monto);
        o.setFechaCreacion(LocalDateTime.now());
        o.setFechaVencimiento(insc.getFechaLimitePago());
        o.setEstado(OrdenPagoEstados.PENDIENTE);
        o.setMetodo("NEQUI");
        o.setReferencia("TEMP-" + UUID.randomUUID());
        o = ordenPagoRepository.save(o);

        String ref = generarReferencia(insc.getEstudiante().getIdEstudiante(), o.getId());
        o.setReferencia(ref);
        o = ordenPagoRepository.save(o);

        registrarHistorial(o, "ORDEN_CREADA", estudiantePersona.getEmail(), "Orden Nequi creada. Monto: " + monto);
        return o;
    }

    private String generarReferencia(Integer idEstudiante, Integer idOrden) {
        return String.format(Locale.ROOT, "SMC-%s-%d-%06d",
                LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE),
                idEstudiante,
                idOrden);
    }

    @Transactional
    public ComprobantePago guardarComprobante(Persona estudiantePersona, Integer idOrden,
            MultipartFile archivo,
            LocalDateTime fechaHoraPagoReportada,
            String telefonoPagador,
            String ultimos4Digitos,
            BigDecimal valorReportado,
            String observacionEstudiante) throws Exception {

        OrdenPago orden = ordenPagoRepository.findById(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        validarTitularOrden(estudiantePersona, orden);
        if (!OrdenPagoEstados.PENDIENTE.equals(orden.getEstado())) {
            throw new IllegalStateException("Solo se puede subir comprobante mientras la orden está pendiente (sin comprobante previo)");
        }
        if (orden.getFechaVencimiento() != null && orden.getFechaVencimiento().isBefore(LocalDate.now())) {
            throw new IllegalStateException("La orden está vencida");
        }
        if (valorReportado == null
                || orden.getMonto().compareTo(valorReportado.setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new IllegalArgumentException("El valor declarado debe coincidir exactamente con el monto de la orden");
        }

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Debe adjuntar un archivo (JPG, PNG o PDF)");
        }
        String ct = archivo.getContentType();
        if (ct == null || !mimePermitido(ct)) {
            throw new IllegalArgumentException("Formato no permitido. Use JPG, PNG o PDF)");
        }
        long maxBytes = 5 * 1024 * 1024L;
        if (archivo.getSize() > maxBytes) {
            throw new IllegalArgumentException("El archivo supera el tamaño máximo permitido (5 MB)");
        }

        byte[] bytes = archivo.getBytes();
        String hash = sha256Hex(bytes);

        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path sub = base.resolve("comprobantes")
                .resolve(String.valueOf(LocalDate.now().getYear()))
                .resolve(String.format(Locale.ROOT, "%02d", LocalDate.now().getMonthValue()));
        Files.createDirectories(sub);

        String ext = extensionSegura(archivo.getOriginalFilename(), ct);
        String stored = UUID.randomUUID() + ext;
        Path destino = sub.resolve(stored);
        Files.write(destino, bytes);

        String relativa = Paths.get("comprobantes")
                .resolve(String.valueOf(LocalDate.now().getYear()))
                .resolve(String.format(Locale.ROOT, "%02d", LocalDate.now().getMonthValue()))
                .resolve(stored)
                .toString()
                .replace('\\', '/');

        ComprobantePago c = new ComprobantePago();
        c.setOrdenPago(orden);
        c.setNombreArchivoOriginal(archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "comprobante");
        c.setRutaAlmacenamiento(relativa);
        c.setContentType(ct);
        c.setTamanoBytes(archivo.getSize());
        c.setHashSha256(hash);
        c.setFechaSubida(LocalDateTime.now());
        c.setFechaHoraPagoReportada(fechaHoraPagoReportada);
        c.setTelefonoPagador(blankToNull(telefonoPagador));
        c.setUltimos4Digitos(sanearUltimos4(ultimos4Digitos));
        c.setValorReportado(valorReportado != null ? valorReportado.setScale(2, RoundingMode.HALF_UP) : null);
        c.setObservacionEstudiante(blankToNull(observacionEstudiante));
        c = comprobantePagoRepository.save(c);

        orden.setEstado(OrdenPagoEstados.COMPROBANTE_CARGADO);
        ordenPagoRepository.save(orden);

        registrarHistorial(orden, "COMPROBANTE_SUBIDO", estudiantePersona.getEmail(),
                "Archivo subido. Hash SHA-256: " + hash);
        return c;
    }

    private static String sanearUltimos4(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String d = s.replaceAll("\\D", "");
        if (d.length() < 4) {
            return d;
        }
        return d.substring(d.length() - 4);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static boolean mimePermitido(String contentType) {
        String c = contentType.toLowerCase(Locale.ROOT).trim();
        return MIME_PERMITIDOS.contains(c);
    }

    private static String extensionSegura(String nombreOriginal, String contentType) {
        String n = nombreOriginal != null ? nombreOriginal.toLowerCase(Locale.ROOT) : "";
        if (n.endsWith(".pdf") || "application/pdf".equalsIgnoreCase(contentType)) {
            return ".pdf";
        }
        if (n.endsWith(".png") || "image/png".equalsIgnoreCase(contentType)) {
            return ".png";
        }
        return ".jpg";
    }

    private static String sha256Hex(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(md.digest(data));
    }

    @Transactional(readOnly = true)
    public Path resolverArchivoComprobante(ComprobantePago c) {
        return Paths.get(uploadDir).toAbsolutePath().normalize().resolve(c.getRutaAlmacenamiento());
    }

    @Transactional(readOnly = true)
    public OrdenPago obtenerOrdenPorIdParaEstudiante(Integer idOrden, Persona persona) {
        OrdenPago o = ordenPagoRepository.findByIdWithDetalle(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
        validarTitularOrden(persona, o);
        return o;
    }

    @Transactional(readOnly = true)
    public OrdenPago obtenerOrdenPorIdParaAdmin(Integer idOrden) {
        return ordenPagoRepository.findByIdWithDetalle(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
    }

    @Transactional
    public void marcarEnRevision(Integer idOrden, String adminUsername) {
        OrdenPago o = ordenPagoRepository.findById(idOrden).orElseThrow();
        if (!OrdenPagoEstados.COMPROBANTE_CARGADO.equals(o.getEstado())) {
            throw new IllegalStateException("Solo órdenes con comprobante cargado pueden pasar a revisión");
        }
        o.setEstado(OrdenPagoEstados.EN_REVISION);
        ordenPagoRepository.save(o);
        registrarHistorial(o, "EN_REVISION", adminUsername, "Revisión iniciada");
    }

    @Transactional
    public void aprobar(Integer idOrden, String adminUsername, String detalle) {
        OrdenPago o = ordenPagoRepository.findById(idOrden).orElseThrow();
        if (!OrdenPagoEstados.COMPROBANTE_CARGADO.equals(o.getEstado())
                && !OrdenPagoEstados.EN_REVISION.equals(o.getEstado())) {
            throw new IllegalStateException("Solo se aprueba desde comprobante cargado o en revisión");
        }
        ComprobantePago c = comprobantePagoRepository.findByOrdenPago_Id(o.getId())
                .orElseThrow(() -> new IllegalStateException("No hay comprobante"));

        if (c.getHashSha256() != null && comprobantePagoRepository.existsMismoArchivoEnOtraOrdenAprobada(
                c.getHashSha256(), OrdenPagoEstados.APROBADO, o.getId())) {
            throw new IllegalStateException(
                    "Este archivo ya fue usado en otra orden aprobada. Verifique duplicados o fraude.");
        }

        if (c.getValorReportado() == null || o.getMonto().compareTo(c.getValorReportado()) != 0) {
            throw new IllegalStateException(
                    "El valor reportado no coincide con el monto de la orden. Revise antes de aprobar.");
        }

        o.setEstado(OrdenPagoEstados.APROBADO);
        ordenPagoRepository.save(o);

        var activo = estadoInscripcionRepository.findByCodigo(InscripcionEstados.ACTIVO)
                .orElseThrow(() -> new IllegalStateException("Estado ACTIVO no configurado"));
        Inscripcion insc = o.getInscripcion();
        insc.setEstado(activo);
        inscripcionRepository.save(insc);

        registrarHistorial(o, "APROBADO", adminUsername, detalle != null ? detalle : "Pago aprobado. Acceso activado.");
    }

    @Transactional
    public void rechazar(Integer idOrden, String adminUsername, String motivo) {
        OrdenPago o = ordenPagoRepository.findById(idOrden).orElseThrow();
        if (!OrdenPagoEstados.COMPROBANTE_CARGADO.equals(o.getEstado())
                && !OrdenPagoEstados.EN_REVISION.equals(o.getEstado())) {
            throw new IllegalStateException("Estado no permitido para rechazo");
        }
        o.setEstado(OrdenPagoEstados.RECHAZADO);
        o.setObservaciones(motivo);
        ordenPagoRepository.save(o);
        registrarHistorial(o, "RECHAZADO", adminUsername, motivo);
    }

    @Transactional
    public int marcarOrdenesVencidas() {
        LocalDate hoy = LocalDate.now();
        List<OrdenPago> lista = ordenPagoRepository.findVencidas(hoy, ACTIVOS);
        for (OrdenPago o : lista) {
            o.setEstado(OrdenPagoEstados.VENCIDO);
            ordenPagoRepository.save(o);
            registrarHistorial(o, "VENCIDO", "SISTEMA", "Plazo de la orden superado");
        }
        return lista.size();
    }

    private void registrarHistorial(OrdenPago orden, String accion, String usuario, String detalle) {
        HistorialPago h = new HistorialPago();
        h.setOrdenPago(orden);
        h.setAccion(accion);
        h.setUsuario(usuario);
        h.setFecha(LocalDateTime.now());
        h.setDetalle(detalle);
        historialPagoRepository.save(h);
    }

    private static void validarTitularOrden(Persona persona, OrdenPago orden) {
        if (orden.getInscripcion() == null || orden.getInscripcion().getEstudiante() == null
                || orden.getInscripcion().getEstudiante().getPersona() == null) {
            throw new IllegalStateException("Datos de orden incompletos");
        }
        if (!persona.getId().equals(orden.getInscripcion().getEstudiante().getPersona().getId())) {
            throw new IllegalStateException("No autorizado");
        }
    }

    /** Parsea fecha y hora de pago desde formulario (campos separados). */
    public static LocalDateTime combinarFechaHoraPago(LocalDate fecha, String hora) {
        if (fecha == null) {
            throw new IllegalArgumentException("Indique la fecha del pago");
        }
        LocalTime t = LocalTime.of(12, 0);
        if (hora != null && !hora.isBlank()) {
            try {
                String[] p = hora.trim().split(":");
                int h = Integer.parseInt(p[0]);
                int m = p.length > 1 ? Integer.parseInt(p[1]) : 0;
                t = LocalTime.of(h, m);
            } catch (Exception e) {
                t = LocalTime.of(12, 0);
            }
        }
        return LocalDateTime.of(fecha, t);
    }
}
