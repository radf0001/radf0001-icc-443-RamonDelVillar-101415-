package org.example.proyecto_final_calidad.repositorio;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.example.proyecto_final_calidad.model.Stock;
import org.example.proyecto_final_calidad.model.TipoMovimiento;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StockRepositorioCustomImpl implements StockRepositorioCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Stock> findByFilters(Long productoId, TipoMovimiento tipo, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Stock> cq = cb.createQuery(Stock.class);
        Root<Stock> root = cq.from(Stock.class);

        List<Predicate> predicates = new ArrayList<>();

        if (productoId != null) {
            predicates.add(cb.equal(root.get("producto").get("id"), productoId));
        }

        if (tipo != null) {
            predicates.add(cb.equal(root.get("tipo"), tipo));
        }

        if (fechaInicio != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), fechaInicio));
        }

        if (fechaFin != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), fechaFin));
        }

        cq.where(cb.and(predicates.toArray(new Predicate[0])));
        cq.orderBy(cb.desc(root.get("fecha"))); // orden por fecha descendente

        return entityManager.createQuery(cq).getResultList();
    }
}
