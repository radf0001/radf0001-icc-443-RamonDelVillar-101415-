package org.example.proyecto_final_calidad.repositorio;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.example.proyecto_final_calidad.model.CategoriaProducto;
import org.example.proyecto_final_calidad.model.Producto;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductoRepositorioCustomImpl implements ProductoRepositorioCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Producto> findByFilters(String searchTerm, CategoriaProducto categoria,
                                           Double minPrecio, Double maxPrecio,
                                           Integer minCantidad, Integer maxCantidad) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Producto> cq = cb.createQuery(Producto.class);
        Root<Producto> root = cq.from(Producto.class);

        List<Predicate> predicates = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String pattern = "%" + searchTerm.toLowerCase() + "%";
            Predicate nombreLike = cb.like(cb.lower(root.get("nombre")), pattern);
            Predicate descripcionLike = cb.like(cb.lower(root.get("descripcion")), pattern);
            predicates.add(cb.or(nombreLike, descripcionLike));
        }

        if (categoria != null) {
            predicates.add(cb.equal(root.get("categoria"), categoria));
        }

        if (minPrecio != null && minPrecio > 0) {
            predicates.add(cb.ge(root.get("precio"), minPrecio));
        }

        if (maxPrecio != null && maxPrecio > 0) {
            predicates.add(cb.le(root.get("precio"), maxPrecio));
        }

        if (minCantidad != null && minCantidad > 0) {
            predicates.add(cb.ge(root.get("cantidad"), minCantidad));
        }

        if (maxCantidad != null && maxCantidad > 0) {
            predicates.add(cb.le(root.get("cantidad"), maxCantidad));
        }

        cq.where(cb.and(predicates.toArray(new Predicate[0])));
        return entityManager.createQuery(cq).getResultList();
    }
}
