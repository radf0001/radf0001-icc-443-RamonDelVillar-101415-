package org.example.proyecto_final_calidad.playwright;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
        webkit = playwright.webkit();
    }

    @Test
    void testProductosPageOnAllBrowsersAndDevices() {
        testLoginAndPage("/productos", "#nombre", "#btn-guardar");
    }

    @Test
    void testDashboardOnAllBrowsersAndDevices() {
        testLoginAndPage("/dashboard", "#dashboard-welcome", null);
    }

    @Test
    void testStockOnAllBrowsersAndDevices() {
        testLoginAndPage("/stock", "#comboProducto", "#registrarMovimientoBtn");
    }

    @Test
    void testUsersOnAllBrowsersAndDevices() {
        testLoginAndPage("/users", "#username", "#saveUser");
    }

    void testLoginAndPage(String path, String checkSelector, String buttonSelector) {
        BrowserType[] browsers = new BrowserType[]{chromium, firefox, webkit};
        String[] devices = new String[]{"iPhone 13", "Pixel 5", "iPad Mini"};

        for (BrowserType browserType : browsers) {
            try (Browser browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(true))) {
                BrowserContext baseContext = browser.newContext();
                Page basePage = baseContext.newPage();
                testPage(basePage, path, checkSelector, buttonSelector, browserType.name(), "Desktop");

                for (String deviceName : devices) {
                    Browser.NewContextOptions options = new Browser.NewContextOptions().setViewportSize(390, 844);
                    if (deviceName.equals("Pixel 5")) options.setViewportSize(393, 851);
                    if (deviceName.equals("iPad Mini")) options.setViewportSize(768, 1024);

                    Page devicePage = browser.newContext(options).newPage();
                    testPage(devicePage, path, checkSelector, buttonSelector, browserType.name(), deviceName);
                }
            }
        }
    }

    void testPage(Page page, String path, String checkSelector, String buttonSelector, String browser, String device) {
        page.navigate("http://localhost:8080/login");

        page.locator("#input-usuario").locator("input").fill("admin");
        page.locator("#input-contrasena").locator("input").fill("admin123");
        page.click("#btn-login");

        // Esperar redirección al dashboard
        page.waitForURL("**/dashboard");

        // Luego navegar según el destino
        if (path.contains("productos")) {
            page.click("#btn-ver-productos");
        } else if (path.contains("stock")) {
            page.click("#btn-control-stock");
        } else if (path.contains("users")) {
            page.click("#btn-ver-usuarios");
        }

        // Esperar URL de destino
        page.waitForURL("**" + path);

        if (checkSelector != null) {
            Locator label = page.locator(checkSelector);
            assertTrue(label.isVisible(), "Elemento " + checkSelector + " no visible en " + browser + " (" + device + ")");
        }

        if (buttonSelector != null) {
            Locator btn = page.locator(buttonSelector);
            assertTrue(btn.isEnabled(), "Botón " + buttonSelector + " no habilitado en " + browser + " (" + device + ")");
        }
    }
}
