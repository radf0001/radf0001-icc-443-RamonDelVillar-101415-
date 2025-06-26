package org.example.proyecto_final_calidad.Servicios;
import org.example.proyecto_final_calidad.model.Producto;
import org.example.proyecto_final_calidad.repositorio.ProductoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoServicio {

    private final ProductoRepositorio repositorio;

    @Autowired
    public ProductoServicio(ProductoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<Producto> findAll() {
        return repositorio.findAll();
    }

    public Producto save(Producto p) {
        return repositorio.save(p);
    }

    public void deleteById(Long id) {
        repositorio.deleteById(id);
    }
}
