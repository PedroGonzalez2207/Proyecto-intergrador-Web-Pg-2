package ec.edu.ups.ppw.dao;

import java.util.List;

import ec.edu.ups.ppw.model.Disponibilidad;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;

@Stateless
public class DisponibilidadDAO {

    @PersistenceContext
    private EntityManager em;

    public void insert(Disponibilidad d) { em.persist(d); }
    public Disponibilidad read(Long id) { return em.find(Disponibilidad.class, id); }
    public Disponibilidad update(Disponibilidad d) { return em.merge(d); }

    public List<Disponibilidad> listByProgramador(Long programadorId) {
        return em.createQuery("SELECT d FROM Disponibilidad d WHERE d.programador.id = :pid AND d.activo = true",
                              Disponibilidad.class)
                 .setParameter("pid", programadorId)
                 .getResultList();
    }
}
