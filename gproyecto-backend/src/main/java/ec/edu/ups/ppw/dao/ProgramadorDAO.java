package ec.edu.ups.ppw.dao;

import java.util.List;

import ec.edu.ups.ppw.model.Programador;
import ec.edu.ups.ppw.model.Rol;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

@Stateless
public class ProgramadorDAO {

    @PersistenceContext
    private EntityManager em;

    public void insert(Programador p) { em.persist(p); }
    public Programador read(Long id) { return em.find(Programador.class, id); }
    public Programador update(Programador p) { return em.merge(p); }

    public Programador readWithUsuario(Long id) {
        try {
            return em.createQuery(
                    "SELECT p FROM Programador p JOIN FETCH p.usuario WHERE p.id = :id",
                    Programador.class
            ).setParameter("id", id)
             .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public Programador readActivoWithUsuario(Long id) {
        try {
            return em.createQuery(
                    "SELECT p FROM Programador p JOIN FETCH p.usuario " +
                    "WHERE p.id = :id AND p.usuario.activo = true AND p.usuario.rol = :rol",
                    Programador.class
            ).setParameter("id", id)
             .setParameter("rol", Rol.Programador)
             .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public Programador findByUsuarioId(Long usuarioId) {
        try {
            return em.createQuery(
                    "SELECT p FROM Programador p WHERE p.usuario.id = :uid",
                    Programador.class
            ).setParameter("uid", usuarioId)
             .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public Programador findByUsuarioFirebaseUid(String firebaseUid) {
        try {
            return em.createQuery(
                    "SELECT p FROM Programador p JOIN FETCH p.usuario WHERE p.usuario.firebaseUid = :fuid",
                    Programador.class
            ).setParameter("fuid", firebaseUid)
             .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public Programador findActivoByUsuarioFirebaseUid(String firebaseUid) {
        try {
            return em.createQuery(
                    "SELECT p FROM Programador p JOIN FETCH p.usuario " +
                    "WHERE p.usuario.firebaseUid = :fuid AND p.usuario.activo = true AND p.usuario.rol = :rol",
                    Programador.class
            ).setParameter("fuid", firebaseUid)
             .setParameter("rol", Rol.Programador)
             .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public List<Programador> listAll() {
        return em.createQuery(
                "SELECT DISTINCT p FROM Programador p JOIN FETCH p.usuario ORDER BY p.id DESC",
                Programador.class
        ).getResultList();
    }

    public List<Programador> listActivos() {
        return em.createQuery(
                "SELECT DISTINCT p FROM Programador p JOIN FETCH p.usuario " +
                "WHERE p.usuario.activo = true AND p.usuario.rol = :rol " +
                "ORDER BY p.id DESC",
                Programador.class
        ).setParameter("rol", Rol.Programador)
         .getResultList();
    }
}
