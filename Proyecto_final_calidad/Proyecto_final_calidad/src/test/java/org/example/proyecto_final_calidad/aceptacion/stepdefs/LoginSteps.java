package org.example.proyecto_final_calidad.aceptacion.stepdefs;

import com.microsoft.playwright.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;

import static org.junit.Assert.assertTrue;

public class LoginSteps extends SpringIntegrationTest {

    private static Playwright playwright;
    private static Browser browser;

    private BrowserContext context;
    private Page page;

    private static final boolean SHOW_BROWSER_UI = false;
    private static final int DEFAULT_TIMEOUT = 30000;

    @Before
    public void setup() {
        if (playwright == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(!SHOW_BROWSER_UI));
        }

        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1280, 1024)
                .setIgnoreHTTPSErrors(true));
        context.setDefaultTimeout(DEFAULT_TIMEOUT);
        page = context.newPage();
    }

    @After
    public void teardown() {
        if (page != null && !page.isClosed()) page.close();
        if (context != null) context.close();
    }

    @Given("el usuario está en la página de login")
    public void abrirLogin() {
        page.navigate("http://localhost:8080/login");
    }

    @When("ingresa usuario {string} y contraseña {string}")
    public void ingresarCredenciales(String usuario, String contrasena) {
        fillVaadinField("#input-usuario", usuario);
        fillVaadinField("#input-contrasena", contrasena);
    }

    @When("hace clic en {string}")
    public void hacerClick(String botonId) {
        page.locator("#" + botonId).click();
    }

    @Then("debería ver el dashboard")
    public void verificarDashboard() {
        page.waitForURL("**/dashboard");
        assertTrue(page.url().contains("dashboard"));
    }

    // ================= MÉTODOS AUXILIARES =================
    private void fillVaadinField(String selector, String value) {
        Locator field = page.locator(selector);
        field.waitFor(new Locator.WaitForOptions().setTimeout(DEFAULT_TIMEOUT));

        try {
            Locator input = field.locator("input").first();
            if (input.count() > 0) {
                input.fill(value);
                return;
            }
        } catch (Exception ignored) {}

        try {
            field.click();
            field.fill(value);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo llenar el campo " + selector, e);
        }
    }
}
