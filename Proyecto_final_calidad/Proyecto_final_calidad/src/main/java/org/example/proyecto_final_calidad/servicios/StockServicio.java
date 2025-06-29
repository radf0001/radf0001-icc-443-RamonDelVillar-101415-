package org.example.proyecto_final_calidad.servicios;

import org.example.proyecto_final_calidad.model.Producto;
import org.example.proyecto_final_calidad.model.Stock;
import org.example.proyecto_final_calidad.model.TipoMovimiento;

import java.time.LocalDateTime;
import java.util.List;

public interface StockServicio {
    void registrarMovimiento(Producto producto, int cantidad, TipoMovimiento tipo, String usuario);
    List<Stock> obtenerHistorialPorProducto(Long productoId);
    List<Stock> obtenerHistorial();
    List<Stock> filtrarMovimientos(Long productoId, TipoMovimiento tipo, LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
