package org.example.proyecto_final_calidad.playwright;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class CompatibilidadTest {
    static Playwright playwright;
    BrowserType chromium, firefox, webkit;

    @BeforeAll
    static void launchPlaywright() {
        playwright = Playwright.create();
    }

    @AfterAll
    static void closePlaywright() {
        playwright.close();
    }

    @BeforeEach
    void setupBrowsers() {
        chromium = playwright.chromium();
        firefox = playwright.firefox();
        webkit   = playwright.webkit();
    }

    @Test
    void testProductosPageOnAllBrowsers() {
        for (BrowserType browserType : new BrowserType[]{chromium, firefox, webkit}) {
            try (Browser browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(true));
                 Page page = browser.newPage()) {
                page.navigate("http://localhost:8080/productos");
                // Esperar al grid
                Locator grid = page.locator("vaadin-grid");
                assertTrue(grid.isVisible(), "Grid no visible en " + browserType.name());

                // Verificar que el campo Nombre está presente
                Locator nombreField = page.locator("vaadin-text-field[label=\"Nombre\"] input");
                assertTrue(nombreField.isVisible(), "Campo Nombre no visible en " + browserType.name());

                // Verificar que el botón Guardar está presente
                Locator btnGuardar = page.locator("vaadin-button:has-text(\"Guardar\")");
                assertTrue(btnGuardar.isEnabled(), "Botón Guardar no habilitado en " + browserType.name());
            }
        }
    }
}
