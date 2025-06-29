package org.example.proyecto_final_calidad.repositorio;

import org.example.proyecto_final_calidad.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepositorio extends JpaRepository<Producto, Long>, ProductoRepositorioCustom {
}
