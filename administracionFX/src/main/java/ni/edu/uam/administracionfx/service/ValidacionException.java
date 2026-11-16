package ni.edu.uam.administracionfx.service;

/** Error de negocio con un mensaje listo para mostrarle al usuario. */
public class ValidacionException extends RuntimeException {
    public ValidacionException(String message) {
        super(message);
    }
}
