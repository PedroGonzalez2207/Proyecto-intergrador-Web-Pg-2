package ec.edu.ups.ppw.dao;

import java.util.List;

import ec.edu.ups.ppw.model.Asesoria;
import ec.edu.ups.ppw.model.EstadoAsesoria;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;

@Stateless
public class AsesoriaDAO {

    @PersistenceContext
    private EntityManager em;

    public void insert(Asesoria a) { em.persist(a); }
    public Asesoria read(Long id) { return em.find(Asesoria.class, id); }
    public Asesoria update(Asesoria a) { return em.merge(a); }

    public List<Asesoria> listByCliente(Long clienteId) {
        return em.createQuery("SELECT a FROM Asesoria a WHERE a.cliente.id = :cid ORDER BY a.fechaInicio DESC",
                              Asesoria.class)
                 .setParameter("cid", clienteId)
                 .getResultList();
    }

    public List<Asesoria> listByProgramador(Long programadorId) {
        return em.createQuery("SELECT a FROM Asesoria a WHERE a.programador.id = :pid ORDER BY a.fechaInicio DESC",
                              Asesoria.class)
                 .setParameter("pid", programadorId)
                 .getResultList();
    }

    public List<Asesoria> listByEstado(EstadoAsesoria estado) {
        return em.createQuery("SELECT a FROM Asesoria a WHERE a.estado = :e ORDER BY a.fechaInicio DESC",
                              Asesoria.class)
                 .setParameter("e", estado)
                 .getResultList();
    }
}
