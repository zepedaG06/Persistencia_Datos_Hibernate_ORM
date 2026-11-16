package ni.edu.uam.administracionfx.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DAO genérico. Los errores se propagan como {@link DaoException} para que
 * la capa de servicio / la interfaz puedan avisarle al usuario.
 */
public class ImplDao implements IDao {

    @Override
    public <T> List<T> getAll(String namedQuery, Class<T> clazz) {
        return findByNamedQuery(namedQuery, clazz, Map.of());
    }

    @Override
    public <T> List<T> findByNamedQuery(String namedQuery, Class<T> clazz, Map<String, Object> params) {
        EntityManager em = EntityManagerAdmin.getEntityManager();
        try {
            TypedQuery<T> query = em.createNamedQuery(namedQuery, clazz);
            params.forEach(query::setParameter);
            return query.getResultList();
        } catch (Exception ex) {
            throw new DaoException("No se pudo consultar " + namedQuery, ex);
        } finally {
            em.close();
        }
    }

    @Override
    public <T> Optional<T> findOne(String namedQuery, Class<T> clazz, Map<String, Object> params) {
        return findByNamedQuery(namedQuery, clazz, params).stream().findFirst();
    }

    @Override
    public <T> void insert(T entity) {
        EntityManager em = EntityManagerAdmin.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entity);
            em.flush();
            tx.commit();
        } catch (Exception ex) {
            rollback(tx);
            throw new DaoException("No se pudo guardar el registro", ex);
        } finally {
            em.close();
        }
    }

    @Override
    public <T> T update(T entity) {
        EntityManager em = EntityManagerAdmin.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T ent = em.merge(entity);
            em.flush();
            tx.commit();
            return ent;
        } catch (Exception ex) {
            rollback(tx);
            throw new DaoException("No se pudo actualizar el registro", ex);
        } finally {
            em.close();
        }
    }

    @Override
    public <T> void remove(T entity) {
        EntityManager em = EntityManagerAdmin.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.remove(em.merge(entity));
            em.flush();
            tx.commit();
        } catch (Exception ex) {
            rollback(tx);
            throw new DaoException("No se pudo eliminar el registro", ex);
        } finally {
            em.close();
        }
    }

    @Override
    public <T> T findById(Class<T> clazz, Object id) {
        EntityManager em = EntityManagerAdmin.getEntityManager();
        try {
            return em.find(clazz, id);
        } catch (Exception ex) {
            throw new DaoException("No se pudo buscar el registro", ex);
        } finally {
            em.close();
        }
    }

    private static void rollback(EntityTransaction tx) {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
    }
}
