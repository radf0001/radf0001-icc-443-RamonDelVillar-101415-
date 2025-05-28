package edu.pucmm;

public class Empleado {
    private String nombre;
    private double tarifaHora;
    private TipoEmpleado tipo;

    public Empleado(String nombre, double tarifaHora, TipoEmpleado tipo) {
        if (nombre == null || tarifaHora < 0 || tipo == null) {
            throw new IllegalArgumentException("Datos inválidos para el empleado");
        }
        this.nombre = nombre;
        this.tarifaHora = tarifaHora;
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public double getTarifaHora() {
        return tarifaHora;
    }

    public TipoEmpleado getTipo() {
        return tipo;
    }
}