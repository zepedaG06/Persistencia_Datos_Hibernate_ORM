package ni.edu.uam.administracionfx.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IDao {

    <T> List<T> getAll(String namedQuery, Class<T> clazz);

    /** Ejecuta una named query con parámetros nombrados (":nombre"). */
    <T> List<T> findByNamedQuery(String namedQuery, Class<T> clazz, Map<String, Object> params);

    /** Igual que {@link #findByNamedQuery} pero devuelve solo el primer resultado. */
    <T> Optional<T> findOne(String namedQuery, Class<T> clazz, Map<String, Object> params);

    <T> void insert(T entity);

    <T> T update(T entity);

    <T> void remove(T entity);

    /** El id debe ser del mismo tipo que la llave primaria (p. ej. UUID). */
    <T> T findById(Class<T> clazz, Object id);

}
