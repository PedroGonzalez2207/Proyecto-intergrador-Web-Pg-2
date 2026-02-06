package ec.edu.ups.ppw.dao;

import java.util.List;

import ec.edu.ups.ppw.model.Usuario;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;

@Stateless
public class UsuarioDAO {

    @PersistenceContext
    private EntityManager em;

    public void insert(Usuario u) { em.persist(u); }
    public Usuario read(Long id) { return em.find(Usuario.class, id); }
    public Usuario update(Usuario u) { return em.merge(u); }

    public Usuario findByEmail(String email) {
        if (email == null) return null;
        String e = email.trim().toLowerCase();
        try {
            return em.createQuery("SELECT u FROM Usuario u WHERE LOWER(u.email) = :email", Usuario.class)
                     .setParameter("email", e)
                     .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public Usuario findByFirebaseUid(String firebaseUid) {
        if (firebaseUid == null) return null;
        try {
            return em.createQuery("SELECT u FROM Usuario u WHERE u.firebaseUid = :uid", Usuario.class)
                     .setParameter("uid", firebaseUid)
                     .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public List<Usuario> getAll() {
        return em.createQuery("SELECT u FROM Usuario u ORDER BY u.id", Usuario.class)
                 .getResultList();
    }
}
