package ni.edu.uam.administracionfx.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Crea la EntityManagerFactory la primera vez que se necesita.
 * Si falla (p. ej. PostgreSQL apagado), se puede volver a intentar
 * sin reiniciar la aplicación.
 */
public class EntityManagerAdmin {

    private static EntityManagerFactory emf;

    private static synchronized EntityManagerFactory factory() {
        if (emf == null || !emf.isOpen()) {
            try {
                emf = Persistence.createEntityManagerFactory("default");
            } catch (RuntimeException ex) {
                throw new DaoException("No se pudo conectar con la base de datos", ex);
            }
        }
        return emf;
    }

    public static EntityManager getEntityManager() {
        return factory().createEntityManager();
    }

    public static synchronized void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
