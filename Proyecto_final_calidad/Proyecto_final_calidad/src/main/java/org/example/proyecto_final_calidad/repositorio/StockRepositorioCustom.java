package org.example.proyecto_final_calidad.repositorio;

import org.example.proyecto_final_calidad.model.Stock;
import org.example.proyecto_final_calidad.model.TipoMovimiento;

import java.time.LocalDateTime;
import java.util.List;

public interface StockRepositorioCustom {

    List<Stock> findByFilters(Long productoId, TipoMovimiento tipo, LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
