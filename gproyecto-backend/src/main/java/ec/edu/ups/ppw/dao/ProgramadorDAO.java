package ec.edu.ups.ppw.dao;

import ec.edu.ups.ppw.model.Programador;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;

@Stateless
public class ProgramadorDAO {

    @PersistenceContext
    private EntityManager em;

    public void insert(Programador p) { em.persist(p); }
    public Programador read(Long id) { return em.find(Programador.class, id); }
    public Programador update(Programador p) { return em.merge(p); }

    public Programador findByUsuarioId(Long usuarioId) {
        try {
            return em.createQuery("SELECT p FROM Programador p WHERE p.usuario.id = :uid", Programador.class)
                     .setParameter("uid", usuarioId)
                     .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }
}
