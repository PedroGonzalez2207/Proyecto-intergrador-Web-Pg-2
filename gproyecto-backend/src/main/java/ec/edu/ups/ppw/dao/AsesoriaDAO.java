package ec.edu.ups.ppw.dao;

import java.time.LocalDateTime;
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

    public Asesoria readFull(Long id) {
        try {
            return em.createQuery(
                "SELECT a FROM Asesoria a " +
                "JOIN FETCH a.cliente " +
                "JOIN FETCH a.programador " +
                "JOIN FETCH a.programador.usuario " +
                "WHERE a.id = :id",
                Asesoria.class
            ).setParameter("id", id)
             .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Asesoria> listByCliente(Long clienteId) {
        return em.createQuery(
                "SELECT a FROM Asesoria a " +
                "WHERE a.cliente.id = :cid " +
                "ORDER BY a.fechaInicio DESC",
                Asesoria.class
        ).setParameter("cid", clienteId)
         .getResultList();
    }

    public List<Asesoria> listByProgramador(Long programadorId) {
        return em.createQuery(
                "SELECT a FROM Asesoria a " +
                "WHERE a.programador.id = :pid " +
                "ORDER BY a.fechaInicio DESC",
                Asesoria.class
        ).setParameter("pid", programadorId)
         .getResultList();
    }

    public List<Asesoria> listByProgramadorAndEstado(Long programadorId, EstadoAsesoria estado) {
        return em.createQuery(
                "SELECT a FROM Asesoria a " +
                "WHERE a.programador.id = :pid AND a.estado = :e " +
                "ORDER BY a.fechaInicio DESC",
                Asesoria.class
        ).setParameter("pid", programadorId)
         .setParameter("e", estado)
         .getResultList();
    }

    public boolean existsOverlappingProgramador(Long programadorId, LocalDateTime inicio, LocalDateTime fin) {
        Long count = em.createQuery(
                "SELECT COUNT(a) FROM Asesoria a " +
                "WHERE a.programador.id = :pid " +
                "AND a.estado <> :rech " +
                "AND ( :inicio < a.fechaFin AND :fin > a.fechaInicio )",
                Long.class
        ).setParameter("pid", programadorId)
         .setParameter("rech", EstadoAsesoria.Rechazada)
         .setParameter("inicio", inicio)
         .setParameter("fin", fin)
         .getSingleResult();

        return count != null && count > 0;
    }
}
