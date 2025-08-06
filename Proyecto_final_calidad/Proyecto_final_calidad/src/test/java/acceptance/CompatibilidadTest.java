package acceptance;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import java.nio.file.Paths;
import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = org.example.proyecto_final_calidad.ProyectoFinalCalidadApplication.class)
public class CompatibilidadTest {

    static Playwright playwright;
    static Browser browser;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(500));
    }

    @BeforeEach
    void setupPage() {
        page = browser.newPage();
        page.waitForTimeout(Duration.ofSeconds(15).toMillis());
        try {
            login();
            page.waitForLoadState();
        } catch (Exception e) {
            System.out.println("Error al conectar: " + e.getMessage());
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("error_screenshot.png")));
            throw e;
        }
    }

    void login() {
        page.navigate("http://localhost:8080/login");
        System.out.println("Navegando a login: " + page.url());
        page.waitForLoadState();

        Locator usuarioField = page.locator("#input-usuario >> input");
        Locator contrasenaField = page.locator("#input-contrasena >> input");
        Locator loginButton = page.locator("#btn-login");

        if (!usuarioField.isVisible()) {
            System.out.println("Campo #input-usuario no encontrado. HTML: " + page.locator("#input-usuario").innerHTML());
        }
        if (!contrasenaField.isVisible()) {
            System.out.println("Campo #input-contrasena no encontrado. HTML: " + page.locator("#input-contrasena").innerHTML());
        }
        if (!loginButton.isVisible()) {
            System.out.println("Botón #btn-login no encontrado. HTML: " + page.locator("#btn-login").innerHTML());
        }

        usuarioField.fill("admin");
        contrasenaField.fill("admin123");
        loginButton.click();

        // Espera la redirección a /dashboard
        page.waitForURL("**/dashboard", new Page.WaitForURLOptions().setTimeout(60000));
        System.out.println("Redirigido a: " + page.url());
    }

    @Test
    void testProductosPageOnAllBrowsersAndDevices() {
        page.navigate("http://localhost:8080/productos");
        Assertions.assertTrue(page.locator("#grid-productos").isVisible(), "La grilla de productos no es visible");
    }

    @Test
    void testUserManagementPage() {
        page.navigate("http://localhost:8080/users");
        page.locator("#username >> input").fill("newuser");
        page.locator("#password >> input").fill("newpass123");
        page.locator("#email >> input").fill("newuser@example.com");
        page.locator("#role").selectOption("ADMINISTRADOR");
        page.click("#saveUser");
        page.waitForTimeout(Duration.ofSeconds(2).toMillis());
        Assertions.assertTrue(page.locator("#gridUsers").isVisible(), "La grilla de usuarios no es visible");
        Assertions.assertTrue(page.locator("#gridUsers").getByText("newuser").isVisible(), "El usuario newuser no aparece en la grilla");
    }

    @AfterEach
    void teardownPage() {
        if (page != null) {
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshot.png")));
            page.close();
        }
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}