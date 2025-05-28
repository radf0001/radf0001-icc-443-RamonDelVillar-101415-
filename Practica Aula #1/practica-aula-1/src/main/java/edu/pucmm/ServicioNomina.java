package edu.pucmm;

public class ServicioNomina {

    public double calcularSalarioSemanal(Empleado empleado, double horas, boolean autorizado) {
        if (empleado == null) throw new IllegalArgumentException("Empleado nulo");
        if (horas < 0) throw new IllegalArgumentException("Horas negativas");

        double salario;
        double tarifa = empleado.getTarifaHora();

        if (empleado.getTipo() == TipoEmpleado.FULL_TIME) {
            if (horas > 40) {
                salario = (40 * tarifa) + ((horas - 40) * tarifa * 1.5);
            } else {
                salario = horas * tarifa;
            }
        } else {
            salario = horas * tarifa; // PART_TIME no recibe horas extra
        }

        if (horas > 38) {
            salario += 500; // Bono por puntualidad
        }

        if (salario > 20000 && !autorizado) {
            return -1;
        }

        return salario;
    }
}
