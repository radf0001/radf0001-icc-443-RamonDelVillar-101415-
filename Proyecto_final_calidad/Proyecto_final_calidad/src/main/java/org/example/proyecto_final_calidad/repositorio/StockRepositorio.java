package org.example.proyecto_final_calidad.repositorio;

import org.example.proyecto_final_calidad.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockRepositorio extends JpaRepository<Stock, Long>, StockRepositorioCustom {
    List<Stock> findByProductoIdOrderByFechaDesc(Long productoId);
    List<Stock> findAllByOrderByFechaDesc();
    int countStockByProductoIsNull();
}
