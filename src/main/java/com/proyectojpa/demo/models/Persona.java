package com.proyectojpa.demo.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import jakarta.persistence.Transient;

import com.proyectojpa.demo.validation.OnAdminCreate;
import com.proyectojpa.demo.validation.OnRegistro;

@Entity
@Table(name = "persona")
public class Persona implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona")
    private Integer id;

    @NotBlank(message = "El documento no puede estar vacío")
    @Pattern(regexp = "\\d{5,}", message = "El documento debe contener solo números (mínimo 5)")
    @Column(name = "no_documento", nullable = false, unique = true)
    private String documento;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Pattern(regexp = "CC|TI|CE", message = "Tipo de documento: CC, TI o CE")
    @Column(name = "tipo_documento", nullable = false)
    private String tipoDocumento;

    @NotBlank(message = "El género es obligatorio")
    @Pattern(regexp = "Femenino|Masculino|Otro", message = "Género no válido")
    @Column(name = "genero", nullable = false)
    private String genero;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    @Column(name = "nombre_persona", nullable = false)
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Pattern(
            regexp = "^[\\w.%+-]+@(?i)(gmail\\.com|hotmail\\.com|hotmail\\.es|outlook\\.com|yahoo\\.com|yahoo\\.es|live\\.com|icloud\\.com|proton\\.me)$",
            message = "Correo inválido o dominio no permitido (use Gmail, Hotmail, Outlook, Yahoo, Live, iCloud o Proton)")
    @Column(name = "email_persona")
    private String email;

    @NotBlank(message = "La dirección no puede estar vacía")
    @Pattern(
            regexp = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ#.,\\-\\s]+$",
            message = "La dirección debe ser alfanumérica (puede incluir # . , -)")
    @Column(name = "Direccion_Persona")
    private String direccion;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^3\\d{9}$", message = "Teléfono: 10 dígitos numéricos, debe iniciar con 3")
    @Column(name = "telefono_persona")
    private String telefono;

    @NotNull(message = "Debe asignar un rol")
    @Column(name = "id_rol")
    private Integer rolId;

    @NotBlank(message = "La contraseña es obligatoria", groups = { OnRegistro.class, OnAdminCreate.class })
    @Pattern(
            regexp = "^$|^(?=.*[0-9])(?=.*[A-Z])(?=.*[^a-zA-Z0-9\\s]).{8,}$",
            message = "Mínimo 8 caracteres, una mayúscula, un número y un carácter especial")
    @Column(name = "contrasena", nullable = false)
    private String contrasena;

    /**
     * Históricamente se modeló como 1-1, pero en BD pueden existir varias filas {@code estudiante}
     * para la misma persona (duplicados). OneToMany evita el fallo de Hibernate al cargar el admin.
     * Solo REMOVE: al guardar una {@code Persona} (p. ej. acudiente con rol 4) no se deben persistir
     * filas en {@code estudiante} por cascade; esas filas se crean explícitamente vía {@code EstudianteRepository}.
     */
    @OneToMany(mappedBy = "persona", cascade = CascadeType.REMOVE)
    private List<Estudiante> estudiantes = new ArrayList<>();

    // ✅ Campos transitorios para validación de formulario
    @Transient
    private String confirmarContrasena;

    @Transient
    private Boolean aceptaTerminos;

    /** Solo formulario (TI): datos del acudiente van a {@code persona} del acudiente + {@link com.proyectojpa.demo.models.Acudiente#parentesco}. */
    @Transient
    private String tutorNombre;

    @Transient
    private String tutorTelefono;

    @Transient
    private String tutorEmail;

    @Transient
    private String tutorTipoDocumento;

    @Transient
    private String tutorDocumento;

    @Transient
    private String tutorGenero;

    @Transient
    private String tutorDireccion;

    @Transient
    private String tutorAutorizacion;

    // Getters y setters existentes
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public Integer getRolId() { return rolId; }
    public void setRolId(Integer rolId) { this.rolId = rolId; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    // ✅ Getters y setters para los campos transitorios
    public String getConfirmarContrasena() { return confirmarContrasena; }
    public void setConfirmarContrasena(String confirmarContrasena) { this.confirmarContrasena = confirmarContrasena; }

    public Boolean getAceptaTerminos() { return aceptaTerminos; }
    public void setAceptaTerminos(Boolean aceptaTerminos) { this.aceptaTerminos = aceptaTerminos; }

    public String getTutorNombre() { return tutorNombre; }
    public void setTutorNombre(String tutorNombre) { this.tutorNombre = tutorNombre; }

    public String getTutorTelefono() { return tutorTelefono; }
    public void setTutorTelefono(String tutorTelefono) { this.tutorTelefono = tutorTelefono; }

    public String getTutorEmail() { return tutorEmail; }
    public void setTutorEmail(String tutorEmail) { this.tutorEmail = tutorEmail; }

    public String getTutorTipoDocumento() { return tutorTipoDocumento; }
    public void setTutorTipoDocumento(String tutorTipoDocumento) { this.tutorTipoDocumento = tutorTipoDocumento; }

    public String getTutorDocumento() { return tutorDocumento; }
    public void setTutorDocumento(String tutorDocumento) { this.tutorDocumento = tutorDocumento; }

    public String getTutorGenero() { return tutorGenero; }
    public void setTutorGenero(String tutorGenero) { this.tutorGenero = tutorGenero; }

    public String getTutorDireccion() { return tutorDireccion; }
    public void setTutorDireccion(String tutorDireccion) { this.tutorDireccion = tutorDireccion; }

    public String getTutorAutorizacion() { return tutorAutorizacion; }
    public void setTutorAutorizacion(String tutorAutorizacion) { this.tutorAutorizacion = tutorAutorizacion; }

    public List<Estudiante> getEstudiantes() {
        return estudiantes;
    }

    public void setEstudiantes(List<Estudiante> estudiantes) {
        this.estudiantes = estudiantes;
    }

    /** Perfil "principal" si hay varios registros duplicados: el de menor {@code id_estudiante}. */
    public Estudiante getEstudiante() {
        if (estudiantes == null || estudiantes.isEmpty()) {
            return null;
        }
        return estudiantes.stream()
                .min(Comparator.comparing(Estudiante::getIdEstudiante, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(estudiantes.get(0));
    }

    public void setEstudiante(Estudiante estudiante) {
        if (estudiantes == null) {
            estudiantes = new ArrayList<>();
        }
        estudiantes.clear();
        if (estudiante != null) {
            estudiante.setPersona(this);
            estudiantes.add(estudiante);
        }
    }
}

