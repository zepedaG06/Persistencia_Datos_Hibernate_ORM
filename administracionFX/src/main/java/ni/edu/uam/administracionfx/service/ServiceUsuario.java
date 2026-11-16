package ni.edu.uam.administracionfx.service;

import ni.edu.uam.administracionfx.model.Usuario;
import ni.edu.uam.administracionfx.util.PasswordUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Reglas de negocio de usuarios: inicio de sesión, registro y CRUD.
 */
public class ServiceUsuario extends ServiceGeneric {

    private static final Pattern CORREO =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern NOMBRE_USUARIO =
            Pattern.compile("^[A-Za-z0-9._-]{4,30}$");
    public static final int MIN_PASSWORD = 6;

    public ServiceUsuario() {
        super();
    }

    // ---------------------------------------------------------------- consultas

    public List<Usuario> getAll() {
        return dao.getAll("Usuario.findAll", Usuario.class);
    }

    public Usuario getById(String id) {
        return dao.findById(Usuario.class, UUID.fromString(id));
    }

    // ------------------------------------------------------------ autenticación

    /**
     * Valida credenciales usando nombre de usuario o correo.
     * Si la contraseña estaba guardada en texto plano (registros antiguos),
     * la convierte a hash en ese momento.
     */
    public Usuario autenticar(String login, String password) {
        if (vacio(login) || vacio(password)) {
            throw new ValidacionException("Ingresa tu usuario y tu contraseña.");
        }
        Usuario u = dao.findOne("Usuario.findByLogin", Usuario.class, Map.of("valor", login.trim()))
                .orElseThrow(() -> new ValidacionException("Usuario o contraseña incorrectos."));

        if (!PasswordUtil.verificar(password, u.getPassword())) {
            throw new ValidacionException("Usuario o contraseña incorrectos.");
        }
        if (!PasswordUtil.esHash(u.getPassword())) {
            u.setPassword(PasswordUtil.hash(password));
            u = dao.update(u);
        }
        return u;
    }

    // ---------------------------------------------------------------- creación

    /** Registro público desde la pantalla "Crear cuenta". */
    public Usuario registrar(Usuario u, String password, String confirmacion) {
        if (!vacio(password) && !password.equals(confirmacion)) {
            throw new ValidacionException("Las contraseñas no coinciden.");
        }
        return crear(u, password);
    }

    /** Alta desde el panel de administración. */
    public Usuario crear(Usuario u, String password) {
        normalizar(u);
        validarDatos(u);
        validarPassword(password);
        validarUnicos(u);
        u.setPassword(PasswordUtil.hash(password));
        dao.insert(u);
        return u;
    }

    // ------------------------------------------------------------ actualización

    /**
     * Actualiza datos del usuario. Si {@code nuevaPassword} viene vacío
     * se conserva la contraseña actual.
     */
    public Usuario actualizar(Usuario u, String nuevaPassword) {
        if (u.getId() == null) {
            throw new ValidacionException("El usuario no existe.");
        }
        normalizar(u);
        validarDatos(u);
        validarUnicos(u);

        Usuario actual = dao.findById(Usuario.class, u.getId());
        if (actual == null) {
            throw new ValidacionException("El usuario ya no existe en la base de datos.");
        }
        if (vacio(nuevaPassword)) {
            u.setPassword(actual.getPassword());
        } else {
            validarPassword(nuevaPassword);
            u.setPassword(PasswordUtil.hash(nuevaPassword));
        }
        if (u.getFechaRegistro() == null) {
            u.setFechaRegistro(actual.getFechaRegistro());
        }
        return dao.update(u);
    }

    // --------------------------------------------------------------- eliminación

    public void eliminar(Usuario u) {
        dao.remove(u);
    }

    // -------------------------------------------------------------- validaciones

    private void normalizar(Usuario u) {
        u.setPrimerNombre(limpiar(u.getPrimerNombre()));
        u.setSegundoNombre(limpiar(u.getSegundoNombre()));
        u.setPrimerApellido(limpiar(u.getPrimerApellido()));
        u.setSegundoApellido(limpiar(u.getSegundoApellido()));
        u.setUsuario(limpiar(u.getUsuario()));
        String correo = limpiar(u.getCorreo());
        u.setCorreo(correo == null ? null : correo.toLowerCase());
    }

    private void validarDatos(Usuario u) {
        if (vacio(u.getPrimerNombre())) throw new ValidacionException("El primer nombre es obligatorio.");
        if (vacio(u.getPrimerApellido())) throw new ValidacionException("El primer apellido es obligatorio.");
        if (vacio(u.getUsuario())) throw new ValidacionException("El nombre de usuario es obligatorio.");
        if (!NOMBRE_USUARIO.matcher(u.getUsuario()).matches()) {
            throw new ValidacionException(
                    "El usuario debe tener de 4 a 30 caracteres: letras, números, punto, guion o guion bajo.");
        }
        if (vacio(u.getCorreo())) throw new ValidacionException("El correo es obligatorio.");
        if (!CORREO.matcher(u.getCorreo()).matches()) {
            throw new ValidacionException("El correo no tiene un formato válido.");
        }
    }

    private void validarPassword(String password) {
        if (vacio(password) || password.length() < MIN_PASSWORD) {
            throw new ValidacionException(
                    "La contraseña debe tener al menos " + MIN_PASSWORD + " caracteres.");
        }
    }

    private void validarUnicos(Usuario u) {
        Optional<Usuario> porUsuario =
                dao.findOne("Usuario.findByUsuario", Usuario.class, Map.of("valor", u.getUsuario()));
        if (porUsuario.isPresent() && !porUsuario.get().getId().equals(u.getId())) {
            throw new ValidacionException("El nombre de usuario «" + u.getUsuario() + "» ya está en uso.");
        }
        Optional<Usuario> porCorreo =
                dao.findOne("Usuario.findByCorreo", Usuario.class, Map.of("valor", u.getCorreo()));
        if (porCorreo.isPresent() && !porCorreo.get().getId().equals(u.getId())) {
            throw new ValidacionException("Ya existe una cuenta con el correo " + u.getCorreo() + ".");
        }
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    private static String limpiar(String s) {
        return vacio(s) ? null : s.trim();
    }
}
