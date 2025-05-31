package edu.pucmm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CarritoTest {
    private List<ItemCarrito> listaItems;
    private Carrito carrito;

    @BeforeEach
    void setUp() {
        listaItems = new ArrayList<>();
        listaItems.add(new ItemCarrito(new Producto("Arroz (Selecto), primera", 44.13), 10)); // 441.30
        listaItems.add(new ItemCarrito(new Producto("Habichuela roja (José Beta), corta, primera", 87), 1)); // 87.00
        listaItems.add(new ItemCarrito(new Producto("Habichuela negra (Arroyo loro negro), primera", 64.50), 100)); // 6450.00
        listaItems.add(new ItemCarrito(new Producto("Huevos (Consumo), primera, grande", 7.48), 30)); // 224.40

        carrito = new Carrito();
        for (ItemCarrito item : listaItems) {
            carrito.agregarProducto(item.getProducto(), item.getCantidad());
        }
    }

    @Test
    @DisplayName("Calcula correctamente el total del carrito con los productos iniciales")
    void testCalcularTotal() {
        double totalEsperado = 441.30 + 87.00 + 6450.00 + 224.40;
        assertEquals(totalEsperado, carrito.calcularTotal());
    }

    @Test
    @DisplayName("Elimina un producto correctamente del carrito")
    void testEliminarProducto() {
        carrito.eliminarProducto("Arroz (Selecto), primera");
        assertEquals(3, carrito.getItems().size());
        assertFalse(carrito.getItems().stream().anyMatch(
                item -> item.getProducto().getNombre().equals("Arroz (Selecto), primera")));
    }

    @Test
    @DisplayName("Modifica la cantidad de un producto y recalcula el total correctamente")
    void testModificarCantidad() {
        carrito.modificarCantidad("Huevos (Consumo), primera, grande", 60);
        double totalEsperado = 441.30 + 87.00 + 6450.00 + 448.80;
        assertEquals(totalEsperado, carrito.calcularTotal());
    }

    @Test
    @DisplayName("Agrega un nuevo producto al carrito y actualiza el total")
    void testAgregarProductoNuevo() {
        carrito.agregarProducto(new Producto("Queso", 80), 1);
        assertEquals(5, carrito.getItems().size());
        double totalEsperado = 441.30 + 87.00 + 6450.00 + 224.40 + 80.00;
        assertEquals(totalEsperado, carrito.calcularTotal());
    }

    @Test
    @DisplayName("Agrega unidades adicionales de un producto existente y actualiza el total")
    void testAgregarProductoExistente() {
        carrito.agregarProducto(new Producto("Arroz (Selecto), primera", 44.13), 5);
        double subtotalArroz = 15 * 44.13; // 661.95
        double totalEsperado = subtotalArroz + 87.00 + 6450.00 + 224.40;
        assertEquals(4, carrito.getItems().size());
        assertEquals(totalEsperado, carrito.calcularTotal());
    }
}
