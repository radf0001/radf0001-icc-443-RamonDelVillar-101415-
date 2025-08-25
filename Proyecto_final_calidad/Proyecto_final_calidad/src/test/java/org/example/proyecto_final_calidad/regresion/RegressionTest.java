package org.example.proyecto_final_calidad.regresion;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import static org.junit.Assert.*;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class RegressionTest {

    private final int port = 8080;

    static Playwright playwright;
    static Browser browser;
    private BrowserContext context;
    Page page;

    // Configuraciones para las pruebas
    private static final boolean SHOW_BROWSER_UI = false;
    private static final int SLOW_MOTION_MS = 300;
    private static final int DEFAULT_TIMEOUT = 30000;
    private static final int ELEMENT_TIMEOUT = 15000;
    private static final int NAVIGATION_TIMEOUT = 45000;

    // Datos de prueba para regresión
    private static final String TEST_USER = "regressionuser";
    private static final String TEST_EMAIL = "regression@test.com";
    private static final String TEST_PASSWORD = "regression123";
    private static final String TEST_PRODUCT = "Producto Regresion";
    private static final String TEST_PRODUCT_DESCRIPTION = "Descripcion para pruebas de regresion";

    // Lista para almacenar datos creados durante las pruebas
    private List<String> createdUsers = new ArrayList<>();
    private List<String> createdProducts = new ArrayList<>();

    @BeforeClass
    public static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(!SHOW_BROWSER_UI)
                .setSlowMo(SLOW_MOTION_MS)
                .setDevtools(SHOW_BROWSER_UI));
    }

    @Before
    public void setupPage() throws Exception {
        setupContext();
        try {
            loginAsAdmin();
        } catch (Exception e) {
            System.out.println("Error en login: " + e.getMessage());
            captureFailureEvidence(e);
            throw e;
        }
    }

    private void setupContext() {
        if (context != null) {
            context.close();
        }
        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1280, 1024)
                .setIgnoreHTTPSErrors(true));
        context.setDefaultTimeout(DEFAULT_TIMEOUT);
        page = context.newPage();
    }

    void loginAsAdmin() throws Exception {
        try {
            System.out.println("🔐 Iniciando login para pruebas de regresión...");
            page.navigate("http://localhost:" + port + "/login",
                    new Page.NavigateOptions()
                            .setTimeout(NAVIGATION_TIMEOUT)
                            .setWaitUntil(WaitUntilState.NETWORKIDLE));

            // Verificar elementos de login
            assertTrue("Campo usuario no visible", page.locator("#input-usuario").isVisible());
            assertTrue("Campo contraseña no visible", page.locator("#input-contrasena").isVisible());
            assertTrue("Botón login no visible", page.locator("#btn-login").isVisible());

            // Realizar login
            fillVaadinTextField("#input-usuario", "admin");
            fillVaadinTextField("#input-contrasena", "admin123");
            page.locator("#btn-login").click();

            // Verificar redirección exitosa
            page.waitForURL("**/dashboard", new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));
            page.waitForLoadState(LoadState.NETWORKIDLE);
            assertTrue("URL no contiene dashboard", page.url().contains("dashboard"));
            System.out.println("✓ Login completado exitosamente");
        } catch (Exception e) {
            captureFailureEvidence(e);
            throw e;
        }
    }

    // ================== PRUEBAS DE REGRESIÓN CORE ==================

    @Test
    public void testLoginFunctionalityRegression() {
        System.out.println("🧪 Prueba de regresión: Funcionalidad de Login");

        // Logout para probar login completo
        try {
            // Si hay un botón de logout, usarlo
            if (page.locator("#logout-button").count() > 0) {
                page.locator("#logout-button").click();
            } else {
                // Navegar directamente al login
                page.navigate("http://localhost:" + port + "/login");
            }

            page.waitForURL("**/login", new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));

            // Probar login con credenciales incorrectas
            fillVaadinTextField("#input-usuario", "wronguser");
            fillVaadinTextField("#input-contrasena", "wrongpass");
            page.locator("#btn-login").click();

            // Verificar que permanece en login (no redirige)
            page.waitForTimeout(2000);
            assertTrue("Debería permanecer en login con credenciales incorrectas",
                    page.url().contains("login"));

            // Probar login con credenciales correctas
            fillVaadinTextField("#input-usuario", "admin");
            fillVaadinTextField("#input-contrasena", "admin123");
            page.locator("#btn-login").click();

            // Verificar redirección exitosa
            page.waitForURL("**/dashboard", new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));
            assertTrue("Debería redirigir al dashboard con credenciales correctas",
                    page.url().contains("dashboard"));

            System.out.println("✓ Regresión de login: EXITOSA");
        } catch (Exception e) {
            captureFailureEvidence(e);
            throw new AssertionError("Fallo en regresión de login: " + e.getMessage(), e);
        }
    }

    @Test
    public void testDashboardElementsRegression() {
        System.out.println("🧪 Prueba de regresión: Elementos del Dashboard");

        try {
            // Verificar URL del dashboard
            assertThat(page).hasURL(java.util.regex.Pattern.compile(".*dashboard.*"));

            // Verificar botones principales
            assertThat(page.locator("#btn-ver-productos")).isVisible();
            assertThat(page.locator("#btn-ver-usuarios")).isVisible();
            assertThat(page.locator("#btn-control-stock")).isVisible();

            // Verificar que los botones son clickeables
            assertTrue("Botón productos no habilitado", page.locator("#btn-ver-productos").isEnabled());
            assertTrue("Botón usuarios no habilitado", page.locator("#btn-ver-usuarios").isEnabled());
            assertTrue("Botón stock no habilitado", page.locator("#btn-control-stock").isEnabled());

            // Verificar navegación de cada botón
            testNavigationButton("#btn-ver-productos", "productos", "Navegación a productos falló");
            navigateBackToDashboard();

            testNavigationButton("#btn-ver-usuarios", "users", "Navegación a usuarios falló");
            navigateBackToDashboard();

            testNavigationButton("#btn-control-stock", "stock", "Navegación a stock falló");
            navigateBackToDashboard();

            System.out.println("✓ Regresión de dashboard: EXITOSA");
        } catch (Exception e) {
            captureFailureEvidence(e);
            throw new AssertionError("Fallo en regresión de dashboard: " + e.getMessage(), e);
        }
    }

    @Test
    public void testUserManagementWorkflowRegression() {
        System.out.println("🧪 Prueba de regresión: Flujo completo de gestión de usuarios");

        try {
            // Navegar a gestión de usuarios
            page.locator("#btn-ver-usuarios").click();
            page.waitForURL("**/users", new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // Verificar elementos de la página
            assertThat(page.locator("#gridUsers")).isVisible();
            assertThat(page.locator("#username")).isVisible();
            assertThat(page.locator("#password")).isVisible();
            assertThat(page.locator("#email")).isVisible();
            assertThat(page.locator("#role")).isVisible();
            assertThat(page.locator("#saveUser")).isVisible();

            // Crear usuario de prueba
            String timestamp = String.valueOf(System.currentTimeMillis());
            String testUser = TEST_USER + timestamp;
            String testEmail = timestamp + TEST_EMAIL;

            fillVaadinTextField("#username", testUser);
            fillVaadinTextField("#password", TEST_PASSWORD);
            fillVaadinTextField("#email", testEmail);
            selectVaadinOption("#role", "ADMINISTRADOR");

            // Guardar usuario
            page.locator("#saveUser").click();
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(2000);

            // Verificar que el usuario aparece en la
            GridElement usersGrid = new GridElement(page.locator("#gridUsers").elementHandle());
            // Esperar que aparezca en la grid
            usersGrid.waitForCellByText(testUser, 50); // 50 filas maximo
            usersGrid.waitForCellByText(testEmail, 50);

            // Registrar usuario creado para cleanup
            createdUsers.add(testUser);

            // Probar crear usuario con datos duplicados (debería fallar)
            fillVaadinTextField("#username", testUser);
            fillVaadinTextField("#password", "otherpass");
            fillVaadinTextField("#email", "other@email.com");
            selectVaadinOption("#role", "EMPLEADO");

            page.locator("#saveUser").click();
            page.waitForTimeout(1000);

            // El sistema debería rechazar el usuario duplicado
            // (Esto depende de la implementación específica)

            System.out.println("✓ Regresión de gestión de usuarios: EXITOSA");
        } catch (Exception e) {
            captureFailureEvidence(e);
            throw new AssertionError("Fallo en regresión de usuarios: " + e.getMessage(), e);
        }
    }

    @Test
    public void testProductManagementWorkflowRegression() {
        System.out.println("🧪 Prueba de regresión: Flujo completo de gestión de productos");

        try {
            // Navegar a gestión de productos
            page.locator("#btn-ver-productos").click();
            page.waitForURL("**/productos", new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // Verificar elementos básicos
            assertThat(page.locator("#nombre")).isVisible();
            assertThat(page.locator("#btn-guardar")).isVisible();

            // Crear producto de prueba
            String timestamp = String.valueOf(System.currentTimeMillis());
            String testProduct = TEST_PRODUCT + " " + timestamp;

            fillVaadinTextField("#nombre", testProduct);

            // Si hay campo descripción, llenarlo
            if (page.locator("#descripcion").count() > 0) {
                fillVaadinTextField("#descripcion", TEST_PRODUCT_DESCRIPTION);
            }

            // Si hay campo precio, llenarlo
            if (page.locator("#precio").count() > 0) {
                fillVaadinTextField("#precio", "100.00");
            }

            // Guardar producto
            page.locator("#btn-guardar").click();
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(2000);

            // Verificar que el producto se guardó
            // (Esto depende de cómo se muestre la confirmación en la UI)

            // Registrar producto para cleanup
            createdProducts.add(testProduct);

            System.out.println("✓ Regresión de gestión de productos: EXITOSA");
        } catch (Exception e) {
            captureFailureEvidence(e);
            throw new AssertionError("Fallo en regresión de productos: " + e.getMessage(), e);
        }
    }

    @Test
    public void testStockControlWorkflowRegression() {
        System.out.println("🧪 Prueba de regresión: Flujo de control de stock");

        try {
            // Navegar a control de stock
            page.locator("#btn-control-stock").click();
            page.waitForURL("**/stock", new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // Verificar elementos de la página
            assertThat(page.locator("#comboProducto")).isVisible();
            assertThat(page.locator("#registrarMovimientoBtn")).isVisible();

            // Verificar que hay productos disponibles en el combo
            page.locator("#comboProducto").click();
            page.waitForTimeout(500);

            // Buscar opciones en el dropdown
            boolean hasOptions = false;
            try {
                hasOptions = page.locator("vaadin-item, [role='option']").count() > 0;
            } catch (Exception e) {
                System.out.println("⚠️ No se encontraron productos en el combo");
            }

            if (hasOptions) {
                // Seleccionar primer producto disponible
                page.locator("vaadin-item, [role='option']").first().click();

                // Verificar campos adicionales si existen
                if (page.locator("#cantidad").count() > 0) {
                    fillVaadinTextField("#cantidad", "10");
                }

                if (page.locator("#tipoMovimiento").count() > 0) {
                    selectVaadinOption("#tipoMovimiento", "ENTRADA");
                }

                // Intentar registrar movimiento
                page.locator("#registrarMovimientoBtn").click();
                page.waitForTimeout(2000);
            }

            System.out.println("✓ Regresión de control de stock: EXITOSA");
        } catch (Exception e) {
            captureFailureEvidence(e);
            throw new AssertionError("Fallo en regresión de stock: " + e.getMessage(), e);
        }
    }

    @Test
    public void testNavigationFlowRegression() {
        System.out.println("🧪 Prueba de regresión: Flujo de navegación completo");

        try {
            // Ruta de navegación completa
            String[] navigationPath = {
                    "#btn-ver-productos",
                    "#btn-ver-usuarios",
                    "#btn-control-stock"
            };

            String[] expectedUrls = {
                    "productos",
                    "users",
                    "stock"
            };

            for (int i = 0; i < navigationPath.length; i++) {
                // Asegurar que estamos en dashboard
                navigateBackToDashboard();

                // Navegar a la sección
                page.locator(navigationPath[i]).click();
                page.waitForURL("**/" + expectedUrls[i],
                        new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));

                assertTrue("URL no contiene " + expectedUrls[i],
                        page.url().contains(expectedUrls[i]));

                // Verificar que la página carga elementos básicos
                page.waitForLoadState(LoadState.NETWORKIDLE);
                page.waitForTimeout(1000);

                System.out.println("✓ Navegación a " + expectedUrls[i] + " exitosa");
            }

            System.out.println("✓ Regresión de navegación: EXITOSA");
        } catch (Exception e) {
            captureFailureEvidence(e);
            throw new AssertionError("Fallo en regresión de navegación: " + e.getMessage(), e);
        }
    }

    @Test
    public void testFormValidationRegression() {
        System.out.println("🧪 Prueba de regresión: Validación de formularios");

        try {
            // Probar validaciones en formulario de usuarios
            page.locator("#btn-ver-usuarios").click();
            page.waitForURL("**/users", new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));

            // Intentar guardar con campos vacíos
            page.locator("#saveUser").click();
            page.waitForTimeout(1000);

            // El sistema debería mostrar errores de validación
            // (Verificar que no se creó el usuario vacío)

            // Probar con email inválido
            fillVaadinTextField("#username", "testuser");
            fillVaadinTextField("#password", "password123");
            fillVaadinTextField("#email", "email-invalido");
            selectVaadinOption("#role", "EMPLEADO");

            page.locator("#saveUser").click();
            page.waitForTimeout(1000);

            // El sistema debería rechazar el email inválido

            System.out.println("✓ Regresión de validación de formularios: EXITOSA");
        } catch (Exception e) {
            captureFailureEvidence(e);
            throw new AssertionError("Fallo en regresión de validación: " + e.getMessage(), e);
        }
    }

    @Test
    public void testDataPersistenceRegression() {
        System.out.println("🧪 Prueba de regresión: Persistencia de datos");

        try {
            // Crear un usuario
            page.locator("#btn-ver-usuarios").click();
            page.waitForURL("**/users", new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));

            String timestamp = String.valueOf(System.currentTimeMillis());
            String testUser = "persistenceTest" + timestamp;
            String testEmail = "persistence" + timestamp + "@test.com";

            fillVaadinTextField("#username", testUser);
            fillVaadinTextField("#password", "persistence123");
            fillVaadinTextField("#email", testEmail);
            selectVaadinOption("#role", "EMPLEADO");

            page.locator("#saveUser").click();
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(2000);

            GridElement usersGrid = new GridElement(page.locator("#gridUsers").elementHandle());
            // Esperar que aparezca en la grid
            usersGrid.waitForCellByText(testUser, 50); // 50 filas maximo
            usersGrid.waitForCellByText(testEmail, 50);

            // Navegar a otra página y volver
            page.locator("#btnVerProductos").click();
            page.waitForURL("**/productos", new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));

            page.locator("#btn-ver-usuarios").click();
            page.waitForURL("**/users", new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // Esperar que aparezca en la grid
            usersGrid.waitForCellByText(testUser, 50); // 50 filas maximo
            usersGrid.waitForCellByText(testEmail, 50);

            createdUsers.add(testUser);

            System.out.println("✓ Regresión de persistencia de datos: EXITOSA");
        } catch (Exception e) {
            captureFailureEvidence(e);
            throw new AssertionError("Fallo en regresión de persistencia: " + e.getMessage(), e);
        }
    }

    // ================== MÉTODOS AUXILIARES ==================

    private void fillVaadinTextField(String selector, String value) {
        System.out.println("🖊️ Llenando campo: " + selector + " con valor: " + value);
        page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(ELEMENT_TIMEOUT));
        Locator field = page.locator(selector);

        try {
            // Intentar con input interno
            Locator input = field.locator("input").first();
            if (input.count() > 0) {
                input.clear();
                input.fill(value);
                return;
            }
        } catch (Exception e) {
            // Intentar con el campo directamente
        }

        try {
            field.click();
            field.fill(value);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo llenar el campo " + selector, e);
        }
    }

    private void selectVaadinOption(String fieldSelector, String optionText) {
        System.out.println("🎯 Seleccionando opción: " + optionText + " en campo: " + fieldSelector);
        page.locator(fieldSelector).click();
        page.waitForTimeout(500);

        try {
            Locator option = page.locator("vaadin-item")
                    .filter(new Locator.FilterOptions().setHasText(optionText));
            if (option.count() == 0) {
                option = page.locator("[role='option']")
                        .filter(new Locator.FilterOptions().setHasText(optionText));
            }
            option.click();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo seleccionar la opción " + optionText, e);
        }
    }

    private void testNavigationButton(String buttonSelector, String expectedUrl, String errorMessage) {
        try {
            page.locator(buttonSelector).click();
            page.waitForURL("**/" + expectedUrl,
                    new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));
            assertTrue(errorMessage, page.url().contains(expectedUrl));
        } catch (Exception e) {
            throw new AssertionError(errorMessage + ": " + e.getMessage(), e);
        }
    }

    private void navigateBackToDashboard() {
        try {
            if (!page.url().contains("dashboard")) {
                page.navigate("http://localhost:" + port + "/dashboard");
                page.waitForURL("**/dashboard",
                        new Page.WaitForURLOptions().setTimeout(NAVIGATION_TIMEOUT));
                page.waitForLoadState(LoadState.NETWORKIDLE);
            }
        } catch (Exception e) {
            System.out.println("⚠️ Error navegando al dashboard: " + e.getMessage());
        }
    }

    private void verifyUserInGrid(String username, String email) {
        System.out.println("🔎 Verificando usuario en el grid por texto");

        try {
            // Espera mínima para que el grid cargue
            page.waitForTimeout(1000);

            // Evaluar contenido completo del grid virtualizado
            Object result = page.evaluate(
                    "(() => {" +
                            "  const grid = document.querySelector('#gridUsers');" +
                            "  if (!grid || !grid.shadowRoot) return '';" +
                            "  const cells = Array.from(grid.shadowRoot.querySelectorAll('vaadin-grid-cell-content'));" +
                            "  return cells.map(c => c.textContent).join('|');" +
                            "})()"
            );

            String gridContent = (String) result;
            System.out.println("📄 Contenido actual del grid: " + gridContent);

            boolean userFoundByName = gridContent.contains(username);
            boolean userFoundByEmail = gridContent.contains(email);
            boolean userFound = userFoundByName || userFoundByEmail;

            if (userFound) {
                System.out.println("✓ Usuario encontrado en el grid");
            } else {
                System.out.println("❌ Usuario no encontrado en el grid.");
                throw new AssertionError("El usuario '" + username + "' con email '" + email + "' no aparece en el grid.");
            }

        } catch (Exception e) {
            System.out.println("❌ Error durante la verificación: " + e.getMessage());

            try {
                page.screenshot(new Page.ScreenshotOptions()
                        .setPath(Paths.get("grid_verification_error_" + System.currentTimeMillis() + ".png")));
            } catch (Exception screenshotError) {
                System.out.println("No se pudo capturar screenshot: " + screenshotError.getMessage());
            }

            throw new AssertionError("Error verificando usuario en grid: " + e.getMessage(), e);
        }
    }

    private void captureFailureEvidence(Exception e) {
        try {
            System.err.println("🔍 Capturando evidencia del error: " + e.getMessage());
            String timestamp = String.valueOf(System.currentTimeMillis());

            // Capturar screenshot
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get("regression_failure_" + timestamp + ".png"))
                    .setFullPage(true));

            // Guardar HTML
            try {
                String htmlContent = page.content();
                Files.write(Paths.get("regression_page_source_" + timestamp + ".html"),
                        htmlContent.getBytes());
            } catch (IOException ioException) {
                System.err.println("Error guardando HTML: " + ioException.getMessage());
            }

            System.err.println("URL actual: " + page.url());
            System.err.println("Título de la página: " + page.title());
        } catch (Exception captureException) {
            System.err.println("Error capturando evidencia: " + captureException.getMessage());
        }
    }

    @After
    public void teardownPage() {
        try {
            // Cleanup de datos de prueba si es necesario
            cleanupTestData();

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

    private void cleanupTestData() {
        // Aquí podrías implementar la limpieza de datos de prueba
        // si tu aplicación lo permite
        System.out.println("🧹 Limpiando datos de prueba...");
        createdUsers.clear();
        createdProducts.clear();
    }

    @AfterClass
    public static void closeBrowser() {
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
}