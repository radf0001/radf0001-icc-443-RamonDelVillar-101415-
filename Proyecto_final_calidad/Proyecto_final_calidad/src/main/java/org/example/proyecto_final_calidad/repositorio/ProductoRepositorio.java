package org.example.proyecto_final_calidad.repositorio;

import org.example.proyecto_final_calidad.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepositorio extends JpaRepository<Producto, Long>, ProductoRepositorioCustom {
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.cantidad < p.stockMinimo")
    long countByCantidadLessThanMinima();
    List<Producto> findAllByEstaActivo(boolean estaActivo);
}
