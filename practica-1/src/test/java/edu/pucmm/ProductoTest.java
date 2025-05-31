package edu.pucmm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProductoTest {
    @Test
    @DisplayName("Verifica getters y setters de la clase 'Producto' correctamente")
    void testGettersAndSetters() {
        Producto p = new Producto("Arroz (Selecto), primera", 44.13);
        assertEquals("Arroz (Selecto), primera", p.getNombre());
        assertEquals(44.13, p.getPrecio());

        p.setNombre("Huevos (Consumo), primera, grande");
        p.setPrecio(7.48);
        assertEquals("Huevos (Consumo), primera, grande", p.getNombre());
        assertEquals(7.48, p.getPrecio());
    }
}
