package ec.edu.ups.ppw.dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import ec.edu.ups.ppw.model.Disponibilidad;
import ec.edu.ups.ppw.model.Modalidad;
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
        return em.createQuery(
                "SELECT d FROM Disponibilidad d " +
                "WHERE d.programador.id = :pid AND d.activo = true " +
                "ORDER BY d.fecha ASC, d.diaSemana ASC, d.horaInicio ASC",
                Disponibilidad.class
        ).setParameter("pid", programadorId)
         .getResultList();
    }

    public List<Disponibilidad> listByProgramadorAndFecha(Long programadorId, LocalDate fecha) {
        return em.createQuery(
                "SELECT d FROM Disponibilidad d " +
                "WHERE d.programador.id = :pid AND d.activo = true AND d.fecha = :f " +
                "ORDER BY d.horaInicio ASC",
                Disponibilidad.class
        ).setParameter("pid", programadorId)
         .setParameter("f", fecha)
         .getResultList();
    }

    public boolean existsOverlappingFecha(Long programadorId, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        Long count = em.createQuery(
                "SELECT COUNT(d) FROM Disponibilidad d " +
                "WHERE d.programador.id = :pid AND d.activo = true AND d.fecha = :f " +
                "AND ( :inicio < d.horaFin AND :fin > d.horaInicio )",
                Long.class
        ).setParameter("pid", programadorId)
         .setParameter("f", fecha)
         .setParameter("inicio", inicio)
         .setParameter("fin", fin)
         .getSingleResult();

        return count != null && count > 0;
    }

    public boolean existsOverlappingDia(Long programadorId, Integer diaSemana, LocalTime inicio, LocalTime fin) {
        Long count = em.createQuery(
                "SELECT COUNT(d) FROM Disponibilidad d " +
                "WHERE d.programador.id = :pid AND d.activo = true AND d.diaSemana = :dia " +
                "AND d.fecha IS NULL " +
                "AND ( :inicio < d.horaFin AND :fin > d.horaInicio )",
                Long.class
        ).setParameter("pid", programadorId)
         .setParameter("dia", diaSemana)
         .setParameter("inicio", inicio)
         .setParameter("fin", fin)
         .getSingleResult();

        return count != null && count > 0;
    }
    
    public boolean existsCoveringSlot(Long programadorId, LocalDate fecha, LocalTime inicio, LocalTime fin, Modalidad modalidad) {
        Long count = em.createQuery(
                "SELECT COUNT(d) FROM Disponibilidad d " +
                "WHERE d.programador.id = :pid AND d.activo = true " +
                "AND d.fecha = :f " +
                "AND d.modalidad = :m " +
                "AND d.horaInicio <= :ini AND d.horaFin >= :fin",
                Long.class
        ).setParameter("pid", programadorId)
         .setParameter("f", fecha)
         .setParameter("m", modalidad)
         .setParameter("ini", inicio)
         .setParameter("fin", fin)
         .getSingleResult();

        return count != null && count > 0;
    }


    public boolean softDelete(Long disponibilidadId) {
        Disponibilidad d = read(disponibilidadId);
        if (d == null) return false;
        d.setActivo(false);
        update(d);
        return true;
    }
}
