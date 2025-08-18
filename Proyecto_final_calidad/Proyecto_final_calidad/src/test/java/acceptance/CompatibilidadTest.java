package acceptance;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import com.microsoft.playwright.options.AriaRole;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = org.example.proyecto_final_calidad.ProyectoFinalCalidadApplication.class)
public class CompatibilidadTest {

    static Playwright playwright;

    private BrowserContext context;
    static Browser browser;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false) // Mantener visible para debug
                .setSlowMo(1000) // Aumentar para ver mejor el flujo
                .setDevtools(true)); // Habilitar devtools
    }

    @BeforeEach
    void setupPage() throws Exception {
        // Configuración inicial
        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1280, 1024)
                .setIgnoreHTTPSErrors(true)); // Ignorar errores SSL si es necesario

        context.setDefaultTimeout(60000);
        page = context.newPage();

        // Interceptar redirecciones
        page.route("**", route -> {
            String url = route.request().url();
            System.out.println("Interceptando: " + url);

            // Detectar posibles bucles
            if (url.contains("login") && route.request().redirectedFrom() != null) {
                System.out.println("Posible bucle detectado en: " + url);
                route.abort();
                return;
            }
            route.resume();
        });

        try {
            login();
        } catch (Exception e) {
            System.out.println("Error en login: " + e.getMessage());
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get("login_error.png"))
                    .setFullPage(true));
            throw e;
        }
    }
    void login() throws Exception {
        try {
            // Configuración inicial
            context.setDefaultTimeout(60000); // Aumentar timeout global a 60s
            page.setDefaultTimeout(60000);

            // 1. Navegar a la página de login
            System.out.println("Navegando a login...");
            Response response = page.navigate("http://localhost:8080/login",
                    new Page.NavigateOptions()
                            .setTimeout(60000)
                            .setWaitUntil(WaitUntilState.NETWORKIDLE));

            // 2. Esperar a que los elementos críticos estén presentes (versión mejorada)
            System.out.println("Esperando elementos de login...");
            Locator usuarioField = waitForAnyLocator(
                    page.getByPlaceholder("Ingresa tu usuario"),
                    page.getByLabel("Usuario"),
                    page.locator("input[name='username'], input[id*='user']")
            );

            Locator contrasenaField = waitForAnyLocator(
                    page.getByPlaceholder("Ingresa tu contraseña"),
                    page.getByLabel("Contraseña"),
                    page.locator("input[type='password'], input[name='password']")
            );

            Locator loginButton = waitForAnyLocator(
                    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Iniciar sesión")),
                    page.locator("button[type='submit'], #btn-login")
            );

            // 3. Rellenar credenciales
            System.out.println("Rellenando credenciales...");
            usuarioField.fill("admin");
            contrasenaField.fill("admin123");

            // 4. Click en login con manejo de navegación mejorado
            System.out.println("Haciendo click en login...");
            loginButton.click();

            page.waitForURL("**/dashboard",
                    new Page.WaitForURLOptions()
                            .setTimeout(60000)
            );
            page.waitForLoadState(LoadState.NETWORKIDLE);


            // 5. Verificar login exitoso con múltiples condiciones
            System.out.println("Verificando login exitoso...");
            waitForAnyCondition(
                    () -> page.url().contains("/dashboard"),
                    () -> page.url().contains("/home"),
                    () -> !page.url().contains("/login"),
                    () -> page.isVisible("vaadin-app-layout")
            );

            System.out.println("Login completado exitosamente en: " + page.url());

        } catch (Exception e) {
            // Capturar evidencia del fallo
            captureFailureEvidence(e);
            throw e;
        }
    }

// Métodos auxiliares:

    private Locator waitForAnyLocator(Locator... locators) {
        for (Locator locator : locators) {
            try {
                locator.waitFor(new Locator.WaitForOptions().setTimeout(15000));
                return locator;
            } catch (Exception e) {
                System.out.println("Locator no encontrado: " + locator);
            }
        }
        throw new RuntimeException("Ninguno de los locators estuvo disponible");
    }

    private void waitForAnyCondition(Callable<Boolean>... conditions) throws Exception {
        long startTime = System.currentTimeMillis();
        long timeout = 60000; // 60 segundos

        while (System.currentTimeMillis() - startTime < timeout) {
            for (Callable<Boolean> condition : conditions) {
                try {
                    if (condition.call()) {
                        return;
                    }
                } catch (Exception e) {
                    // Continuar con la siguiente condición
                }
            }
            page.waitForTimeout(1000); // Esperar 1 segundo entre intentos
        }
        throw new TimeoutException("Ninguna condición se cumplió en el tiempo esperado");
    }

    private void captureFailureEvidence(Exception e) {
        System.err.println("Error durante login: " + e.getMessage());

        // Capturar screenshot
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("failure_" + System.currentTimeMillis() + ".png"))
                .setFullPage(true));

        // Guardar HTML de la página
        try {
            Files.write(Paths.get("page_source.html"), page.content().getBytes());
        } catch (IOException ioException) {
            System.err.println("Error guardando HTML: " + ioException.getMessage());
        }

        // Guardar consola del navegador
        List<String> consoleMessages = new ArrayList<>();
        page.onConsoleMessage(msg -> {
            consoleMessages.add("CONSOLE [" + msg.type() + "]: " + msg.text());
        });
        try {
            Files.write(Paths.get("console_logs.txt"), consoleMessages);
        } catch (IOException ioException) {
            System.err.println("Error guardando logs: " + ioException.getMessage());
        }
    }

    @Test
    void testProductosPageOnAllBrowsersAndDevices() {
        // Verificar que no estamos en un bucle de redirección
        if (page.url().contains("login")) {
            Assertions.fail("La aplicación redirigió de vuelta al login");
        }

        // Navegar con manejo de redirecciones
        Response response = page.waitForNavigation(new Page.WaitForNavigationOptions()
                .setTimeout(60000), () -> {
            // Usar evaluate para navegación en JavaScript puro
            page.evaluate("window.location.href = '/productos';");
        });

        // Verificar código de respuesta
        if (response.status() >= 300 && response.status() < 400) {
            Assertions.fail("Redirección inesperada: " + response.status());
        }

        // Esperar grid con lógica específica para Vaadin
        waitForVaadinGrid("#grid-productos", 60000);
    }

    private void waitForVaadinGrid(String selector, int timeout) {
        // Esperar a que el componente esté en el DOM
        page.waitForSelector(selector, new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(timeout));

        // Esperar a que Vaadin termine de renderizar
        page.waitForFunction(
                "(selector) => { " +
                        "  const grid = document.querySelector(selector); " +
                        "  if (!grid) return false; " +
                        "  if (grid._hasContent) return true; " + // Propiedad de Vaadin
                        "  return Array.from(grid.querySelectorAll('[part~=\"row\"]')).length > 0; " +
                        "}",
                selector,
                new Page.WaitForFunctionOptions().setTimeout(timeout));
    }


    @Test
    void testUserManagementPage() {
        // Navegar y esperar condiciones más robustas
        page.navigate("http://localhost:8080/productos");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForSelector("#gridUsers, vaadin-grid",
                new Page.WaitForSelectorOptions().setTimeout(30000));

        // Usuario - selector más tolerante
        Locator username = page.getByLabel("Usuario")
                .or(page.getByPlaceholder("Usuario"))
                .or(page.locator("[id*='user']"));
        username.waitFor();
        username.fill("newuser");

        // Contraseña - selector más tolerante
        Locator password = page.getByLabel("Contraseña")
                .or(page.getByPlaceholder("Contraseña"))
                .or(page.locator("[id*='pass']"));
        password.waitFor();
        password.fill("newpass123");

        // Email - selector más tolerante
        Locator email = page.getByLabel("Email")
                .or(page.getByPlaceholder("Correo"))
                .or(page.locator("[id*='email']"));
        email.waitFor();
        email.fill("newuser@example.com");

        // Mejor manejo del combo box/select
        Locator role = page.locator("#role, [role='combobox'], vaadin-combo-box")
                .first();
        role.waitFor();

        if (role.getAttribute("tagName").contains("select")) {
            role.selectOption("ADMINISTRADOR");
        } else {
            role.click();
            page.locator("vaadin-item, [role='option']")
                    .filter(new Locator.FilterOptions().setHasText("ADMINISTRADOR"))
                    .first()
                    .click();
        }

        // Guardar con espera explícita
        Locator saveButton = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Guardar"));
        saveButton.waitFor();
        saveButton.click();

        // Esperar a que la operación termine
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000); // Pequeña espera adicional

        // Verificación más robusta del nuevo usuario
        Locator gridUsers = page.locator("#gridUsers, vaadin-grid").first();
        gridUsers.waitFor();

        Locator newUserCell = gridUsers.getByText("newuser")
                .or(gridUsers.getByText("newuser@example.com"));
        newUserCell.waitFor(new Locator.WaitForOptions().setTimeout(15000));

        Assertions.assertTrue(newUserCell.isVisible(),
                "El usuario newuser no aparece en la grilla");
    }

    @AfterEach
    void teardownPage() {
        try {
            // Verificar si hay errores de redirección
            if (page != null) {
                // Capturar estado final
                String finalUrl = page.url();
                System.out.println("URL final: " + finalUrl);

                // Guardar recursos para diagnóstico
                context.tracing().stop(new Tracing.StopOptions()
                        .setPath(Paths.get("trace_" + System.currentTimeMillis() + ".zip")));

                // Cerrar adecuadamente
                page.close();
                context.close();
            }
        } catch (Exception e) {
            System.err.println("Error en teardown: " + e.getMessage());
        }
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}