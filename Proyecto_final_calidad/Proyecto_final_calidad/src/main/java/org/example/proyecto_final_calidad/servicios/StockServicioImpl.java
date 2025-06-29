package org.example.proyecto_final_calidad.servicios;

import org.example.proyecto_final_calidad.model.Producto;
import org.example.proyecto_final_calidad.model.Stock;
import org.example.proyecto_final_calidad.model.TipoMovimiento;
import org.example.proyecto_final_calidad.repositorio.ProductoRepositorio;
import org.example.proyecto_final_calidad.repositorio.StockRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockServicioImpl implements StockServicio {

    @Autowired
    private ProductoRepositorio productoRepositorio;

    @Autowired
    private StockRepositorio stockRepositorio;

    @Override
    public void registrarMovimiento(Producto producto, int cantidad, TipoMovimiento tipo, String usuario) {
        if (tipo == TipoMovimiento.SALIDA && producto.getCantidad() < cantidad) {
            throw new IllegalArgumentException("No hay suficiente stock disponible");
        }

        int nuevaCantidad = tipo == TipoMovimiento.ENTRADA
                ? producto.getCantidad() + cantidad
                : producto.getCantidad() - cantidad;

        producto.setCantidad(nuevaCantidad);
        productoRepositorio.save(producto);

        Stock movimiento = new Stock();
        movimiento.setProducto(producto);
        movimiento.setCantidad(cantidad);
        movimiento.setTipo(tipo);
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setUsuario(usuario);
        stockRepositorio.save(movimiento);
    }

    @Override
    public List<Stock> obtenerHistorialPorProducto(Long productoId) {
        return stockRepositorio.findByProductoIdOrderByFechaDesc(productoId);
    }

    @Override
    public List<Stock> obtenerHistorial() {
        return stockRepositorio.findAllByOrderByFechaDesc();
    }

    @Override
    public List<Stock> filtrarMovimientos(Long productoId, TipoMovimiento tipo, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return stockRepositorio.findByFilters(productoId, tipo, fechaInicio, fechaFin);
    }

}
