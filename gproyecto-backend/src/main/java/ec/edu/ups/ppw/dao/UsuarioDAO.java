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
        try {
            return em.createQuery("SELECT u FROM Usuario u WHERE u.email = :email", Usuario.class)
                     .setParameter("email", email)
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
