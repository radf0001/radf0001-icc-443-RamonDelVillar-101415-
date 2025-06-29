package org.example.proyecto_final_calidad.servicios;

import org.example.proyecto_final_calidad.model.CategoriaProducto;
import org.example.proyecto_final_calidad.model.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoServicio {
    List<Producto> findAll();
    Optional<Producto> findById(Long id);
    Producto save(Producto producto);
    void deleteById(Long id);
    List<Producto> findByFilters(
            String searchTerm,
            CategoriaProducto categoria,
            Double minPrecio,
            Double maxPrecio,
            Integer minCantidad,
            Integer maxCantidad
    );
}
