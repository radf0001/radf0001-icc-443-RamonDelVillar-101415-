package org.example.proyecto_final_calidad.regresion;

import com.microsoft.playwright.ElementHandle;

public class GridElement {
    private final ElementHandle elementHandle;

    public GridElement(ElementHandle elementHandle) {
        this.elementHandle = elementHandle;
    }

    public void scrollToIndex(int index) {
        elementHandle.evaluate("(grid, index) => grid.scrollToIndex(index)", index);
    }

    public void waitForCellByText(String text, int maxScroll) throws InterruptedException {
        for (int i = 0; i < maxScroll; i++) {
            ElementHandle cell = elementHandle.querySelector(String.format("vaadin-grid-cell-content:text('%s')", text));
            if (cell != null) return; // encontrado
            scrollToIndex(i); // scroll fila por fila
            Thread.sleep(100); // pequeño delay para que renderice
        }
        throw new RuntimeException("No se encontró la celda con texto: " + text);
    }
}
