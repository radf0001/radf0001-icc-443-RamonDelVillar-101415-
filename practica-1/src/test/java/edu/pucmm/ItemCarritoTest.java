package edu.pucmm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemCarritoTest {
    @Test
    @DisplayName("Calcula correctamente el subtotal del item del carrito")
    void testSubtotal() {
        Producto producto = new Producto("Habichuela negra (Arroyo loro negro), primera", 64.50);
        ItemCarrito item = new ItemCarrito(producto, 100);
        assertEquals(6450, item.getSubtotal());
    }

    @Test
    @DisplayName("Modifica la cantidad del item correctamente y actualizar el subtotal")
    void testModificarCantidad() {
        Producto producto = new Producto("Habichuela roja (José Beta), corta, primera", 87);
        ItemCarrito item = new ItemCarrito(producto, 1);
        item.setCantidad(100);
        assertEquals(100, item.getCantidad());
        assertEquals(8700, item.getSubtotal());
    }
}
