package org.example.proyecto_final_calidad.servicios;
import org.example.proyecto_final_calidad.model.CategoriaProducto;
import org.example.proyecto_final_calidad.model.Producto;
import org.example.proyecto_final_calidad.repositorio.ProductoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoServicioImpl implements ProductoServicio {

    private final ProductoRepositorio repositorio;

    @Autowired
    public ProductoServicioImpl(ProductoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public List<Producto> findAll() {
        return repositorio.findAll();
    }

    @Override
    public Optional<Producto> findById(Long id) {
        return repositorio.findById(id);
    }

    @Override
    public Producto save(Producto producto) {
        // Validate product before saving
        validateProduct(producto);
        return repositorio.save(producto);
    }

    @Override
    public void deleteById(Long id) {
        repositorio.deleteById(id);
    }

    private void validateProduct(Producto producto) {
        List<String> errors = new ArrayList<>();

        // Name validation
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            errors.add("El nombre no puede estar vacío");
        }

        // Description validation
        if (producto.getDescripcion() == null || producto.getDescripcion().trim().isEmpty()) {
            errors.add("La descripción no puede estar vacía");
        }

        // Category validation
        if (producto.getCategoria() == null) {
            errors.add("La categoría no puede estar vacía");
        }

        // Price validation
        if (producto.getPrecio() < 0) {
            errors.add("El precio no puede ser negativo");
        }

        // Quantity validation
        if (producto.getCantidad() < 0) {
            errors.add("La cantidad no puede ser negativa");
        }

        if (producto.getStockMinimo() < 0) {
            errors.add("El Stock Mínimo no puede ser negativo");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Error de validación: " + String.join(", ", errors));
        }
    }

    @Override
    public List<Producto> findByFilters(
            String searchTerm,
            CategoriaProducto categoria,
            Double minPrecio,
            Double maxPrecio,
            Integer minCantidad,
            Integer maxCantidad
    ) {

        return repositorio.findByFilters(
                searchTerm,
                categoria,
                minPrecio,
                maxPrecio,
                minCantidad,
                maxCantidad
        );
    }
}
