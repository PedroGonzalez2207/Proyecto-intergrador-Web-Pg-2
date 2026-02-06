package ec.edu.ups.ppw.dao;

import java.util.List;

import ec.edu.ups.ppw.model.Proyecto;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;

@Stateless
public class ProyectoDAO {

    @PersistenceContext
    private EntityManager em;

    public void insert(Proyecto p) { em.persist(p); }
    public Proyecto read(Long id) { return em.find(Proyecto.class, id); }
    public Proyecto update(Proyecto p) { return em.merge(p); }

    public List<Proyecto> listByProgramador(Long programadorId) {
        return em.createQuery(
                "SELECT p FROM Proyecto p " +
                "WHERE p.programador.id = :pid AND p.activo = true " +
                "ORDER BY p.id DESC",
                Proyecto.class
        ).setParameter("pid", programadorId)
         .getResultList();
    }

    public boolean softDelete(Long id) {
        Proyecto p = read(id);
        if (p == null) return false;
        p.setActivo(false);
        update(p);
        return true;
    }
}
