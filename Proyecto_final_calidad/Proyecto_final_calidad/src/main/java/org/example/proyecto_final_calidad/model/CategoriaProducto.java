package org.example.proyecto_final_calidad.model;

public enum CategoriaProducto {
    ELECTRONICA("Electrónica"),
    ROPA("Ropa"),
    ALIMENTOS("Alimentos"),
    HOGAR("Hogar"),
    JUGUETES("Juguetes"),
    DEPORTES("Deportes"),
    OTROS("Otros");

    private final String displayName;

    CategoriaProducto(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}