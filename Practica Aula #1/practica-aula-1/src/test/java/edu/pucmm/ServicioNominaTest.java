package edu.pucmm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

public class ServicioNominaTest {
    private ServicioNomina servicio;

    @BeforeEach
    void setup() {
        servicio = new ServicioNomina();
    }

    @Test
    void calcularSalarioSinHorasExtra() {
        Empleado e = new Empleado("Juan", 500, TipoEmpleado.FULL_TIME);
        double salario = servicio.calcularSalarioSemanal(e, 35, true);
        double esperado = 35 * 500;
        assertEquals(esperado, salario);
    }

    @Test
    void calcularSalarioConHorasExtraSoloFullTime() {
        Empleado e = new Empleado("Ana", 400, TipoEmpleado.FULL_TIME);
        double salario = servicio.calcularSalarioSemanal(e, 45, true);
        double esperado = (40 * 400) + (5 * 400 * 1.5) + 500;
        assertEquals(esperado, salario);
    }

    @Test
    void partTimeNoRecibeHorasExtra() {
        Empleado e = new Empleado("Luis", 400, TipoEmpleado.PART_TIME);
        double salario = servicio.calcularSalarioSemanal(e, 45, true);
        double esperado = (45 * 400)+500;
        assertEquals(esperado, salario);
    }

    @Test
    void lanzaExcepcionSiHorasNegativas() {
        Empleado e = new Empleado("Carlos", 400, TipoEmpleado.FULL_TIME);
        assertThrows(IllegalArgumentException.class, () -> {
            servicio.calcularSalarioSemanal(e, -5, false);
        });
    }

    @Test
    void lanzaExcepcionSiEmpleadoEsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            servicio.calcularSalarioSemanal(null, 10, false);
        });
    }

    @Test
    void bonoPorPuntualidadSiTrabajaMasDe38Horas() {
        Empleado e = new Empleado("Pedro", 300, TipoEmpleado.FULL_TIME);
        double salario = servicio.calcularSalarioSemanal(e, 39, false);
        double esperado = 39 * 300 + 500;
        assertEquals(esperado, salario);
    }

    @Test
    void salarioExcedioTopeSinAutorizacion() {
        Empleado e = new Empleado("Maria", 1000, TipoEmpleado.FULL_TIME);
        double salario = servicio.calcularSalarioSemanal(e, 45, false);
        assertTrue(salario == -1, "Salario excede el tope sin autorización");
    }
}
