package ni.edu.uam.administracionfx.dao;

/** Error de acceso a datos (conexión, restricción violada, etc.). */
public class DaoException extends RuntimeException {
    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
