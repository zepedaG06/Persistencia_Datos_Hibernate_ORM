package ni.edu.uam.administracionfx.util;

import ni.edu.uam.administracionfx.model.Usuario;

/** Guarda el usuario que inició sesión mientras la aplicación está abierta. */
public final class Sesion {

    private static Usuario usuarioActual;

    private Sesion() {
    }

    public static void iniciar(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static Usuario getUsuario() {
        return usuarioActual;
    }

    public static boolean activa() {
        return usuarioActual != null;
    }

    public static void cerrar() {
        usuarioActual = null;
    }
}
