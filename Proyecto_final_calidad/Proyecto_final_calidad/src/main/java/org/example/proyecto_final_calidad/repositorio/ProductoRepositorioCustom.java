package org.example.proyecto_final_calidad.repositorio;

import org.example.proyecto_final_calidad.model.CategoriaProducto;
import org.example.proyecto_final_calidad.model.Producto;

import java.util.List;

public interface ProductoRepositorioCustom {

    List<Producto> findByFilters(String searchTerm, CategoriaProducto categoria,
                                    Double minPrecio, Double maxPrecio,
                                    Integer minCantidad, Integer maxCantidad);
}
