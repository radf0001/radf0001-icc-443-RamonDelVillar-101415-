package org.example.proyecto_final_calidad.compatibilidad;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import static org.junit.Assert.*;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class CompatibilidadTest {

    private final int port = 8080;
    static Playwright playwright;
    static Browser browser;
    private BrowserContext context;
    Page page;

    private static final String[][] DEVICE_CONFIGS = {
            {"Desktop", "1280", "1024"},
            {"iPhone 13", "390", "844"},
            {"Pixel 5", "393", "851"},
            {"iPad Mini", "768", "1024"}
    };

    private static final String[] BROWSERS = {"chromium", "firefox", "webkit"};

    private static final boolean SHOW_BROWSER_UI = false;
    private static final int SLOW_MOTION_MS = 500;
    private static final int DEFAULT_TIMEOUT = 30000;
    private static final int ELEMENT_TIMEOUT = 15000;
    private static final int NAVIGATION_TIMEOUT = 45000;

    @BeforeClass
    public static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(!SHOW_BROWSER_UI).setSlowMo(SLOW_MOTION_MS).setDevtools(SHOW_BROWSER_UI));
    }

    @Before
    public void setupPage() throws Exception {
        setupContext(1280, 1024);
        try {
            login();
        } catch (Exception e) {
            System.out.println("Error en login: " + e.getMessage());
            captureFailureEvidence(e);
            throw e;
        }
    }

    private void setupContext(int width, int height) {
        if (context != null) {
            context.close();
        }
        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(width, height).setIgnoreHTTPSErrors(true));
        context.setDefaultTimeout(DEFAULT_TIMEOUT);
        page = context.newPage();
    }

    void login() throws Exception {
        try {
            System.out.println("🔐 Iniciando proceso de login...");
            page.navigate("http://localhost:" + port + "/login", new Page.NavigateOptions().setTimeout(NAVIGATION_TIMEOUT).setWaitUntil(WaitUntilState.NETWORKIDLE));
            System.out.println("🔍 Buscando elementos de login...");
            assertTrue(page.locator("#input-usuario").isVisible());
            assertTrue(page.locator("#input-contrasena").isVisible());
            assertTrue(page.locator("#btn-login").isVisible());
            fillVaadinTextField("#input-usuario", "admin");
            fillVaadinTextField("#input-contrasena", "admin123");
            if (SHOW_BROWSER_UI) page.waitForTimeout(500);
            System.out.println("🚀 Ejecutando login...");
            page.locator("#btn-login").click();
            page.waitForURL("**/dashboard", new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));
            page.waitForLoadState(LoadState.NETWORKIDLE);
            assertTrue(page.url().contains("dashboard"));
            System.out.println("🎉 Login completado exitosamente en: " + page.url());
        } catch (Exception e) {
            captureFailureEvidence(e);
            throw e;
        }
    }

    private void fillVaadinTextField(String selector, String value) {
        System.out.println("🖊️ Llenando campo: " + selector + " con valor: " + value);
        page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(ELEMENT_TIMEOUT));
        Locator field = page.locator(selector);
        try {
            Locator input = field.locator("input").first();
            if (input.count() > 0) {
                input.fill(value);
                System.out.println("✓ Campo llenado usando input interno");
                return;
            }
        } catch (Exception e) {
            System.out.println("⚠️ Falló input interno, intentando click + type");
        }
        try {
            field.click();
            field.fill(value);
            System.out.println("✓ Campo llenado usando click + fill");
        } catch (Exception e) {
            System.out.println("⚠️ Error llenando campo " + selector + ": " + e.getMessage());
            throw new RuntimeException("No se pudo llenar el campo " + selector, e);
        }
    }

    private void selectVaadinOption(String fieldSelector, String optionText) {
        System.out.println("🎯 Seleccionando opción: " + optionText + " en campo: " + fieldSelector);
        page.locator(fieldSelector).click();
        page.waitForTimeout(500);
        Locator option = waitForAnyLocator(ELEMENT_TIMEOUT, page.locator("vaadin-item").filter(new Locator.FilterOptions().setHasText(optionText)), page.locator("[role='option']").filter(new Locator.FilterOptions().setHasText(optionText)), page.locator("vaadin-combo-box-item").filter(new Locator.FilterOptions().setHasText(optionText)), page.getByText(optionText).first());
        option.click();
        System.out.println("✓ Opción seleccionada exitosamente");
    }

    @Test
    public void testUserManagementWithVaadin() {
        System.out.println("🧪 Iniciando prueba de gestión de usuarios con Vaadin...");
        try {
            page.locator("#btn-ver-usuarios").click();
            page.waitForURL("**/users", new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));
            assertTrue(page.url().contains("users"));
            assertTrue(page.locator("#gridUsers").isVisible());
            fillVaadinTextField("#username", "newuser");
            fillVaadinTextField("#password", "newpass123");
            fillVaadinTextField("#email", "newuser@example.com");
            selectVaadinOption("#role", "ADMINISTRADOR");
            if (SHOW_BROWSER_UI) page.waitForTimeout(1000);
            page.locator("#saveUser").click();
            page.waitForLoadState(LoadState.NETWORKIDLE);
            System.out.println("🎉 ¡Prueba de usuarios completada exitosamente!");
        } catch (Exception e) {
            System.err.println("❌ Error en prueba de usuarios: " + e.getMessage());
            captureFailureEvidence(e);
            throw e;
        }
    }

    @Test public void testProductosPage() {
        System.out.println("🧪 Probando página de productos...");
        try {
            page.locator("#btn-ver-productos").click();
            page.waitForURL("**/productos", new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));
            assertThat(page.locator("#nombre")).isVisible();
            assertThat(page.locator("#btn-guardar")).isVisible();
            System.out.println("✓ Página de productos cargada correctamente");
        } catch (Exception e) {
            captureFailureEvidence(e);
            throw e;
        }
    }

    @Test public void testStockPage() {
        System.out.println("🧪 Probando página de stock...");
        try {
            page.locator("#btn-control-stock").click();
            page.waitForURL("**/stock", new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));
            assertThat(page.locator("#comboProducto")).isVisible();
            assertThat(page.locator("#registrarMovimientoBtn")).isVisible();
            System.out.println("✓ Página de stock cargada correctamente");
        } catch (Exception e) {
            captureFailureEvidence(e);
            throw e;
        }
    }

    @Test public void testDashboardElements() {
        System.out.println("🧪 Probando elementos del dashboard...");
        try {
            assertThat(page).hasURL(java.util.regex.Pattern.compile(".*dashboard.*"));
            assertThat(page.locator("#btn-ver-productos")).isVisible();
            assertThat(page.locator("#btn-ver-usuarios")).isVisible();
            assertThat(page.locator("#btn-control-stock")).isVisible();
            System.out.println("✓ Dashboard cargado con todos los elementos");
        } catch (Exception e) {
            captureFailureEvidence(e);
            throw e;
        }
    }

    @Test public void testCrossBrowserCompatibility() {
        System.out.println("🌐 Iniciando pruebas cross-browser...");
        for (String browserName: BROWSERS) {
            System.out.println("🔍 Probando en: " + browserName);
            BrowserType browserType = getBrowserType(browserName);
            try (Browser testBrowser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(true)); BrowserContext testContext = testBrowser.newContext(); Page testPage = testContext.newPage()) {
                testPage.navigate("http://localhost:" + port + "/login");
                testPage.locator("#input-usuario input").fill("admin");
                testPage.locator("#input-contrasena input").fill("admin123");
                testPage.locator("#btn-login").click();
                testPage.waitForURL("**/dashboard");
                assertThat(testPage).hasURL(java.util.regex.Pattern.compile(".*dashboard.*"));
                assertThat(testPage.locator("#btn-ver-productos")).isVisible();
                System.out.println("✓ " + browserName + " - Prueba exitosa");
            } catch (Exception e) {
                System.err.println("✗ " + browserName + " - Error: " + e.getMessage());
                throw e;
            }
        }
        System.out.println("🎉 Todas las pruebas cross-browser completadas");
    }

    private BrowserType getBrowserType(String browserName) {
        switch (browserName.toLowerCase()) {
            case "firefox":
                return playwright.firefox();
            case "webkit":
                return playwright.webkit();
            case "chromium":
            default:
                return playwright.chromium();
        }
    }

    private void captureFailureEvidence(Exception e) {
        try {
            System.err.println("🔍 Capturando evidencia del error: " + e.getMessage());
            String timestamp = String.valueOf(System.currentTimeMillis());
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("failure_" + timestamp + ".png")).setFullPage(true));
            try {
                String htmlContent = page.content();
                Files.write(Paths.get("page_source_" + timestamp + ".html"), htmlContent.getBytes());
            } catch (IOException ioException) {
                System.err.println("Error guardando HTML: " + ioException.getMessage());
            }
            System.err.println("URL actual: " + page.url());
            System.err.println("Título de la página: " + page.title());
        } catch (Exception captureException) {
            System.err.println("Error capturando evidencia: " + captureException.getMessage());
        }
    }

    @After public void teardownPage() {
        try {
            if (page != null && !page.isClosed()) {
                System.out.println("🔚 URL final: " + page.url());
                page.close();
            }
            if (context != null) {
                context.close();
            }
        } catch (Exception e) {
            System.err.println("Error en teardown: " + e.getMessage());
        }
    }

    @AfterClass public static void closeBrowser() {
        try {
            if (browser != null && browser.isConnected()) {
                browser.close();
            }
            if (playwright != null) {
                playwright.close();
            }
        } catch (Exception e) {
            System.err.println("Error cerrando browser/playwright: " + e.getMessage());
        }
    }

    private void fillVaadinField(String fieldName, String value, String...selectors) {
        System.out.println("🖊️ Llenando campo: " + fieldName + " con valor: " + value);
        Locator field = null;
        Exception lastException = null;
        for (String selector: selectors) {
            try {
                field = page.locator(selector);
                if (field.count() > 0 && field.first().isVisible()) {
                    System.out.println("✓ Campo encontrado con selector: " + selector);
                    break;
                }
            } catch (Exception e) {
                lastException = e;
                field = null;
            }
        }
        if (field == null) {
            throw new RuntimeException("No se pudo encontrar el campo: " + fieldName + ". Último error: " + (lastException != null ? lastException.getMessage() : "N/A"));
        }
        try {
            field.waitFor(new Locator.WaitForOptions().setTimeout(ELEMENT_TIMEOUT));
            boolean filled = false;
            try {
                Locator innerInput = field.locator("input").first();
                if (innerInput.count() > 0 && innerInput.isVisible()) {
                    innerInput.clear();
                    innerInput.fill(value);
                    System.out.println("✓ Campo llenado usando input interno");
                    filled = true;
                }
            } catch (Exception e) {
                System.out.println("⚠️ Falló estrategia input interno: " + e.getMessage());
            }
            if (!filled) {
                try {
                    field.click();
                    page.waitForTimeout(300);
                    field.fill("");
                    field.pressSequentially(value, new Locator.PressSequentiallyOptions().setDelay(50));
                    System.out.println("✓ Campo llenado usando pressSequentially");
                    filled = true;
                } catch (Exception e) {
                    System.out.println("⚠️ Falló estrategia pressSequentially: " + e.getMessage());
                }
            }
            if (!filled) {
                try {
                    field.fill(value);
                    System.out.println("✓ Campo llenado usando fill() tradicional");
                    filled = true;
                } catch (Exception e) {
                    System.out.println("⚠️ Falló estrategia fill tradicional: " + e.getMessage());
                }
            }
            if (!filled) {
                try {
                    String selector = selectors[0];
                    page.evaluate(String.format("const element = document.querySelector('%s'); " + "if (element) { " + " element.value = '%s'; " + " element.dispatchEvent(new Event('input', { bubbles: true })); " + " element.dispatchEvent(new Event('change', { bubbles: true })); " + "}", selector.replace("'", "\\'"), value.replace("'", "\\'")));
                    System.out.println("✓ Campo llenado usando JavaScript");
                    filled = true;
                } catch (Exception e) {
                    System.out.println("⚠️ Falló estrategia JavaScript: " + e.getMessage());
                }
            }
            if (!filled) {
                throw new RuntimeException("No se pudo llenar el campo " + fieldName + " con ninguna estrategia");
            }
        } catch (Exception e) {
            captureFailureEvidence(e);
            throw new RuntimeException("Error llenando campo " + fieldName + ": " + e.getMessage(), e);
        }
    }

    private void handleVaadinSelect(String fieldName, String value, String...selectors) {
        System.out.println("🎯 Seleccionando en campo: " + fieldName + " valor: " + value);
        Locator field = waitForAnyLocator(ELEMENT_TIMEOUT, page.locator(selectors[0]), page.locator(selectors.length > 1 ? selectors[1] : selectors[0]), page.locator(selectors.length > 2 ? selectors[2] : selectors[0]));
        try {
            String tagName = field.evaluate("el => el.tagName.toLowerCase()").toString();
            if ("select".equals(tagName)) {
                field.selectOption(value);
                System.out.println("✓ Selección usando selectOption nativo");
                return;
            }
            field.click();
            page.waitForTimeout(500);
            Locator option = waitForAnyLocator(ELEMENT_TIMEOUT, page.locator("vaadin-item").filter(new Locator.FilterOptions().setHasText(value)), page.locator("[role='option']").filter(new Locator.FilterOptions().setHasText(value)), page.locator("vaadin-combo-box-item").filter(new Locator.FilterOptions().setHasText(value)), page.getByText(value).first());
            option.click();
            System.out.println("✓ Selección completada en dropdown Vaadin");
        } catch (Exception e) {
            try {
                String selector = selectors[0];
                page.evaluate(String.format("const element = document.querySelector('%s'); " + "if (element) { " + " element.value = '%s'; " + " element.dispatchEvent(new Event('change', { bubbles: true })); " + "}", selector.replace("'", "\\'"), value.replace("'", "\\'")));
                System.out.println("✓ Selección usando JavaScript fallback");
            } catch (Exception jsError) {
                captureFailureEvidence(e);
                throw new RuntimeException("Error seleccionando en " + fieldName + ": " + e.getMessage(), e);
            }
        }
    }

    @Test
    public void testProductosPageOnAllBrowsersAndDevices() {
        testPageOnAllPlatforms("/productos", "#nombre", "#btn-guardar", (page) -> {
            page.click("#btn-ver-productos");
        });
    }

    @Test public void testDashboardOnAllBrowsersAndDevices() {
        testPageOnAllPlatforms("/dashboard", "#dashboard-welcome", null, null);
    }

    @Test public void testStockOnAllBrowsersAndDevices() {
        testPageOnAllPlatforms("/stock", "#comboProducto", "#registrarMovimientoBtn", (Page page) -> {
            page.click("#btn-control-stock");
        });
    }

    @Test public void testUsersOnAllBrowsersAndDevices() {
        testPageOnAllPlatforms("/users", "#username", "#saveUser", (page) -> {
            page.click("#btn-ver-usuarios");
        });
    }

    @Test public void testUserManagementPageWithAdvancedFeatures() {
        System.out.println("🧪 Iniciando prueba avanzada de gestión de usuarios...");
        if (page.url().contains("login")) {
            fail("La aplicación redirigió de vuelta al login");
        }
        try {
            System.out.println("🔄 Navegando a página de usuarios...");
            page.click("#btn-ver-usuarios");
            page.waitForURL("**/users", new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));
            page.waitForLoadState(LoadState.NETWORKIDLE);
            System.out.println("⏳ Esperando grid de usuarios...");
            waitForVaadinGrid("#gridUsers", 45000);
            System.out.println("📝 Completando formulario de usuario...");
            fillVaadinField("Usuario", "newuser", "#username", "#username input", "vaadin-text-field[id='username'] input", "[id='username'] input");
            fillVaadinField("Contraseña", "newpass123", "#password", "#password input", "vaadin-password-field[id='password'] input", "[id='password'] input");
            fillVaadinField("Email", "newuser@example.com", "#email", "#email input", "vaadin-email-field[id='email'] input", "[id='email'] input");
            handleVaadinSelect("Rol", "ADMINISTRADOR", "#role", "vaadin-select[id='role']", "[id='role']");
            if (SHOW_BROWSER_UI) {
                page.waitForTimeout(1000);
            }
            System.out.println("💾 Guardando usuario...");
            Locator saveButton = waitForAnyLocator(page.locator("#saveUser"), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Guardar")));
            saveButton.waitFor(new Locator.WaitForOptions().setTimeout(ELEMENT_TIMEOUT));
            saveButton.click();
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(2000);
            System.out.println("🎉 ¡Prueba de gestión de usuarios completada exitosamente!");
        } catch (Exception e) {
            System.err.println("❌ Error en prueba de usuarios: " + e.getMessage());
            captureFailureEvidence(e);
            throw e;
        }
    }

    @Test public void testVaadinGridFunctionality() {
        try {
            page.click("#btn-ver-productos");
            page.waitForURL("**/productos", new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));
            waitForVaadinGrid("#grid-productos", DEFAULT_TIMEOUT);
            Locator grid = page.locator("#grid-productos");
            assertTrue("Grid de productos no está visible", grid.isVisible());
            page.waitForFunction("document.querySelector('#grid-productos') && " + "(document.querySelectorAll('#grid-productos [part~=\"row\"]').length > 0 || " + "document.querySelector('#grid-productos').clientHeight > 0)", null, new Page.WaitForFunctionOptions().setTimeout(DEFAULT_TIMEOUT));
            System.out.println("✓ Grid de Vaadin funcionando correctamente");
        } catch (Exception e) {
            System.err.println("❌ Error en prueba de grid Vaadin: " + e.getMessage());
            captureFailureEvidence(e);
            throw e;
        }
    }

    private void testPageOnAllPlatforms(String path, String checkSelector, String buttonSelector, NavigationAction navAction) {
        for (String browserName: BROWSERS) {
            BrowserType browserType = getBrowserType(browserName);
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(!SHOW_BROWSER_UI);
            if (SHOW_BROWSER_UI) {
                launchOptions.setSlowMo(SLOW_MOTION_MS);
            }
            try (Browser testBrowser = browserType.launch(launchOptions)) {
                for (String[] deviceConfig: DEVICE_CONFIGS) {
                    String deviceName = deviceConfig[0];
                    int width = Integer.parseInt(deviceConfig[1]);
                    int height = Integer.parseInt(deviceConfig[2]);
                    System.out.println(String.format("🌐 Probando %s en %s (%s)", path, browserName, deviceName));
                    try (BrowserContext testContext = testBrowser.newContext(new Browser.NewContextOptions().setViewportSize(width, height))) {
                        Page testPage = testContext.newPage();
                        if (SHOW_BROWSER_UI) {
                            testPage.evaluate(String.format("document.title = 'TESTING: %s - %s (%s)';", path, browserName, deviceName));
                        }
                        testSinglePageConfiguration(testPage, path, checkSelector, buttonSelector, navAction, browserName, deviceName);
                        if (SHOW_BROWSER_UI) {
                            testPage.waitForTimeout(1500);
                        }
                    }
                }
            }
        }
    }

    private void testSinglePageConfiguration(Page testPage, String path, String checkSelector, String buttonSelector, NavigationAction navAction, String browser, String device) {
        try {
            testPage.navigate("http://localhost:" + port + "/login");
            testPage.waitForSelector("#input-usuario input", new Page.WaitForSelectorOptions().setTimeout(ELEMENT_TIMEOUT));
            testPage.waitForSelector("#input-contrasena input", new Page.WaitForSelectorOptions().setTimeout(ELEMENT_TIMEOUT));
            testPage.locator("#input-usuario input").fill("admin");
            testPage.locator("#input-contrasena input").fill("admin123");
            testPage.click("#btn-login");
            testPage.waitForURL("**/dashboard", new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));
            if (navAction != null) {
                navAction.navigate(testPage);
                testPage.waitForURL("**" + path, new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));
            }
            if (checkSelector != null) {
                Locator element = testPage.locator(checkSelector);
                element.waitFor(new Locator.WaitForOptions().setTimeout(ELEMENT_TIMEOUT));
                assertTrue(String.format("Elemento %s no visible en %s (%s)", checkSelector, browser, device), element.isVisible());
            }
            if (buttonSelector != null) {
                Locator button = testPage.locator(buttonSelector);
                button.waitFor(new Locator.WaitForOptions().setTimeout(ELEMENT_TIMEOUT));
                assertTrue(String.format("Botón %s no habilitado en %s (%s)", buttonSelector, browser, device), button.isEnabled());
            }
            System.out.println(String.format("✓ Prueba exitosa: %s en %s (%s)", path, browser, device));
        } catch (Exception e) {
            System.err.println(String.format("✗ Fallo en %s - %s (%s): %s", path, browser, device, e.getMessage()));
            throw new AssertionError(String.format("Fallo en %s - %s (%s): %s", path, browser, device, e.getMessage()), e);
        }
    }

    private Locator waitForAnyLocator(int timeout, Locator...locators) {
        Exception lastException = null;
        for (Locator locator: locators) {
            try {
                locator.waitFor(new Locator.WaitForOptions().setTimeout(timeout));
                if (locator.count() > 0) {
                    return locator;
                }
            } catch (Exception e) {
                lastException = e;
                System.out.println("Locator no encontrado: " + locator + " - " + e.getMessage());
            }
        }
        throw new RuntimeException("Ninguno de los locators estuvo disponible. Último error: " + (lastException != null ? lastException.getMessage() : "N/A"));
    }

    private Locator waitForAnyLocator(Locator...locators) {
        return waitForAnyLocator(ELEMENT_TIMEOUT, locators);
    }

    private void waitForAnyCondition(Callable < Boolean > ...conditions) throws Exception {
        long startTime = System.currentTimeMillis();
        long timeout = DEFAULT_TIMEOUT;
        while (System.currentTimeMillis() - startTime < timeout) {
            for (Callable < Boolean > condition: conditions) {
                try {
                    if (condition.call()) {
                        return;
                    }
                } catch (Exception e) {}
            }
            Thread.sleep(1000);
        }
        throw new TimeoutException("Ninguna condición se cumplió en el tiempo esperado");
    }

    private void waitForVaadinGrid(String selector, int timeout) {
        System.out.println("⏳ Esperando grid Vaadin: " + selector);
        page.waitForSelector(selector, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(timeout));
        page.waitForFunction("(selector) => { " + "const grid = document.querySelector(selector); " + "if (!grid) return false; " + "if (grid._hasContent) return true; " + "const rows = grid.querySelectorAll('[part~=\"row\"], vaadin-grid-cell-content'); " + "if (rows.length > 0) return true; " + "return grid.clientHeight > 0 && getComputedStyle(grid).display !== 'none'; " + "}", selector, new Page.WaitForFunctionOptions().setTimeout(timeout));
        System.out.println("✓ Grid Vaadin listo: " + selector);
    }

    @FunctionalInterface interface NavigationAction {
        void navigate(Page page);
    }
}
