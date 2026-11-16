package ni.edu.uam.administracionfx.model;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuario")
@NamedQueries({
        @NamedQuery(
                name = "Usuario.findAll",
                query = "SELECT e FROM Usuario e ORDER BY e.primerApellido, e.primerNombre"
        ),
        @NamedQuery(
                name = "Usuario.findById",
                query = "SELECT e FROM Usuario e WHERE e.id = ?1"
        ),
        @NamedQuery(
                name = "Usuario.findByUsuario",
                query = "SELECT e FROM Usuario e WHERE lower(e.usuario) = lower(:valor)"
        ),
        @NamedQuery(
                name = "Usuario.findByCorreo",
                query = "SELECT e FROM Usuario e WHERE lower(e.correo) = lower(:valor)"
        ),
        @NamedQuery(
                name = "Usuario.findByLogin",
                query = "SELECT e FROM Usuario e "
                        + "WHERE lower(e.usuario) = lower(:valor) OR lower(e.correo) = lower(:valor)"
        )
})
public class Usuario {
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "primer_nombre", nullable = false)
    private String primerNombre;

    @Column(name = "segundo_nombre")
    private String segundoNombre;

    @Column(name = "primer_apellido", nullable = false)
    private String primerApellido;

    @Column(name = "segundo_apellido")
    private String segundoApellido;

    @Column(name = "usuario", nullable = false)
    private String usuario;

    /** Guarda el hash PBKDF2 de la contraseña, nunca el texto plano. */
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "correo")
    private String correo;

    @Column(name = "ruta_foto")
    private String rutaFoto;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @PrePersist
    void antesDeInsertar() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPrimerNombre() {
        return primerNombre;
    }

    public void setPrimerNombre(String primerNombre) {
        this.primerNombre = primerNombre;
    }

    public String getSegundoNombre() {
        return segundoNombre;
    }

    public void setSegundoNombre(String segundoNombre) {
        this.segundoNombre = segundoNombre;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRutaFoto() {
        return rutaFoto;
    }

    public void setRutaFoto(String rutaFoto) {
        this.rutaFoto = rutaFoto;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    /** Nombre completo para mostrar en pantalla (no se persiste). */
    public String getNombreCompleto() {
        StringBuilder sb = new StringBuilder();
        for (String parte : new String[]{primerNombre, segundoNombre, primerApellido, segundoApellido}) {
            if (parte != null && !parte.isBlank()) {
                if (!sb.isEmpty()) sb.append(' ');
                sb.append(parte.trim());
            }
        }
        return sb.toString();
    }

    /** Iniciales para el avatar, p. ej. "JL". */
    public String getIniciales() {
        String a = primerNombre == null || primerNombre.isBlank() ? "" : primerNombre.trim().substring(0, 1);
        String b = primerApellido == null || primerApellido.isBlank() ? "" : primerApellido.trim().substring(0, 1);
        return (a + b).toUpperCase();
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", primerNombre='" + primerNombre + '\'' +
                ", segundoNombre='" + segundoNombre + '\'' +
                ", primerApellido='" + primerApellido + '\'' +
                ", segundoApellido='" + segundoApellido + '\'' +
                ", usuario='" + usuario + '\'' +
                ", correo='" + correo + '\'' +
                ", rutaFoto='" + rutaFoto + '\'' +
                ", fechaRegistro=" + fechaRegistro +
                '}';
    }
}
