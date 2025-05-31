package edu.pucmm;

import java.util.ArrayList;
import java.util.List;

public class Carrito {
    private List<ItemCarrito> items;

    public Carrito() {
        this.items = new ArrayList<>();
    }

    public void agregarProducto(Producto producto, int cantidad) {
        for (ItemCarrito item : items) {
            if (item.getProducto().getNombre().equals(producto.getNombre())) {
                item.setCantidad(item.getCantidad() + cantidad);
                return;
            }
        }
        items.add(new ItemCarrito(producto, cantidad));
    }

    public void eliminarProducto(String nombreProducto) {
        items.removeIf(item -> item.getProducto().getNombre().equals(nombreProducto));
    }

    public void modificarCantidad(String nombreProducto, int nuevaCantidad) {
        for (ItemCarrito item : items) {
            if (item.getProducto().getNombre().equals(nombreProducto)) {
                item.setCantidad(nuevaCantidad);
                return;
            }
        }
    }

    public double calcularTotal() {
        double total = 0;
        for (ItemCarrito item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    public List<ItemCarrito> getItems() {
        return items;
    }
}
